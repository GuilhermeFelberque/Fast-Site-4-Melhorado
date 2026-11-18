// Funções usadas por todas as telas que exigem login

// ===== TIMER DE INATIVIDADE =====
// Tempo que a pessoa pode ficar parada antes de sair sozinha do site.
// (Para demonstrar na apresentação, dá para trocar para 1.)
const MINUTOS_INATIVIDADE = 10;
const AVISO_SEGUNDOS = 60;            // fica vermelho e avisa no último minuto
const AVISAR_SERVIDOR_A_CADA = 60000; // no máximo 1 aviso ao Java por minuto

// Pergunta ao Java quem está logado. Se ninguém, volta para o login.
// Devolve { email, nome, admin }
async function exigirLogin() {
    const resposta = await fetch("/api/usuario");
    if (!resposta.ok) {
        window.location.href = "/index.html";
        return null;
    }
    const usuario = await resposta.json();
    document.querySelectorAll("[data-nome-logado]").forEach(el => el.textContent = usuario.nome);
    document.querySelectorAll("[data-email-logado]").forEach(el => el.textContent = usuario.email);
    // Itens marcados com data-so-admin só aparecem para o administrador
    document.querySelectorAll("[data-so-admin]").forEach(el => el.hidden = !usuario.admin);
    document.querySelectorAll("[data-so-cliente]").forEach(el => el.hidden = usuario.admin);
    document.body.classList.remove("oculto");
    iniciarTimer();   // toda tela logada ganha o timer
    return usuario;
}

// Só para as telas do administrador: quem não é admin volta para o início
async function exigirAdmin() {
    const usuario = await exigirLogin();
    if (usuario && !usuario.admin) {
        window.location.href = "/home.html";
        return null;
    }
    return usuario;
}

// Botão Sair: apaga a sessão e volta para o login
function ligarBotaoSair() {
    const btn = document.getElementById("btnSair");
    if (!btn) return;
    btn.addEventListener("click", async () => {
        await fetch("/api/logout", { method: "POST" });
        window.location.href = "/index.html";
    });
}

// ===== Como o timer funciona =====
// 1) Guarda o horário em que a sessão vai acabar (agora + 10 min).
// 2) A cada segundo, mostra quanto falta (mm:ss).
// 3) Qualquer movimento no site (mouse, clique, tecla, rolagem, toque) joga o fim para agora + 10 min.
// 4) Ao mexer, avisa o Java (GET /api/usuario) para a sessão do servidor também não expirar.
// 5) Se chegar a zero: faz logout e volta para o login com o aviso de inatividade.
let fimDaSessao = 0;
let ultimoAvisoServidor = 0;
let avisouUltimoMinuto = false;
let encerrando = false;

function iniciarTimer() {
    if (document.getElementById("timerSessao")) return;   // já iniciado

    const timer = document.createElement("div");
    timer.id = "timerSessao";
    timer.className = "timer";
    timer.title = "Tempo até sair por inatividade. Mexer no site reinicia o tempo.";
    timer.setAttribute("aria-label", "Tempo restante da sessão");
    timer.innerHTML = '<span aria-hidden="true">⏱</span> <span data-tempo></span>';
    // Coloca no cabeçalho (ou na barra do relatório, ou no canto da tela)
    const lugar = document.querySelector(".topo-direita") || document.querySelector(".barra-relatorio");
    if (lugar) lugar.prepend(timer);
    else { timer.classList.add("timer-flutuante"); document.body.appendChild(timer); }

    ultimoAvisoServidor = Date.now();   // a página acabou de falar com o Java
    reiniciarTimer();

    ["mousemove", "mousedown", "keydown", "scroll", "touchstart", "wheel"].forEach(evento =>
        window.addEventListener(evento, reiniciarTimer, { passive: true }));

    setInterval(atualizarTimer, 1000);
    atualizarTimer();
}

function reiniciarTimer() {
    if (encerrando) return;
    fimDaSessao = Date.now() + MINUTOS_INATIVIDADE * 60 * 1000;
    avisouUltimoMinuto = false;
    const timer = document.getElementById("timerSessao");
    if (timer) timer.classList.remove("acabando");

    // Mantém a sessão do Java viva enquanto a pessoa usa o site
    if (Date.now() - ultimoAvisoServidor > AVISAR_SERVIDOR_A_CADA) {
        ultimoAvisoServidor = Date.now();
        fetch("/api/usuario").then(r => { if (r.status === 401) sairPorInatividade(); }).catch(() => {});
    }
    atualizarTimer();
}

function atualizarTimer() {
    const timer = document.getElementById("timerSessao");
    if (!timer || encerrando) return;

    const restante = Math.max(0, Math.ceil((fimDaSessao - Date.now()) / 1000));
    const mm = String(Math.floor(restante / 60)).padStart(2, "0");
    const ss = String(restante % 60).padStart(2, "0");
    timer.querySelector("[data-tempo]").textContent = `${mm}:${ss}`;

    if (restante <= AVISO_SEGUNDOS) {
        timer.classList.add("acabando");
        if (!avisouUltimoMinuto && typeof toast === "function") {
            avisouUltimoMinuto = true;
            toast("Sua sessão vai expirar em 1 minuto. Mexa no site para continuar.", "erro");
        }
    }
    if (restante === 0) sairPorInatividade();
}

async function sairPorInatividade() {
    if (encerrando) return;
    encerrando = true;
    try { await fetch("/api/logout", { method: "POST" }); } catch { /* sem conexão: sai mesmo assim */ }
    window.location.href = "/index.html?expirou=1";
}
