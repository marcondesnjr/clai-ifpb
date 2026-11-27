document.addEventListener("DOMContentLoaded", function () {
    const formulario = document.getElementById("formulario-estudo");
    const lista = document.getElementById("lista-adaptacoes");
    const botaoAdicionar = document.getElementById("adicionar-adaptacao");

    if (!formulario || !lista || !botaoAdicionar) return;

    // Refaz os nomes (adaptacoesCurriculares[0].adaptacao, [1]...) depois de adicionar/remover
    function atualizarIndices() {
        lista.querySelectorAll(".barreira-item").forEach(function (item, indice) {
            item.querySelectorAll("[data-campo]").forEach(function (campo) {
                campo.name = "adaptacoesCurriculares[" + indice + "]." + campo.dataset.campo;
            });
        });
    }

    function criarCampo(item, rotulo, tag, propriedade) {
        const campo = document.createElement("div");
        campo.className = "campo";

        const label = document.createElement("label");
        label.textContent = rotulo;

        const controle = document.createElement(tag);
        if (tag === "input") controle.type = "text";
        controle.maxLength = tag === "input" ? 255 : 5000;
        controle.dataset.campo = propriedade;

        campo.append(label, controle);
        item.appendChild(campo);
    }

    function criarAdaptacao() {
        const item = document.createElement("div");
        item.className = "barreira-item";

        const titulo = document.createElement("h3");
        titulo.textContent = "Adaptação curricular";
        item.appendChild(titulo);

        criarCampo(item, "Componente curricular", "input", "componenteCurricular");
        criarCampo(item, "Adaptação", "textarea", "adaptacao");
        criarCampo(item, "Justificativa", "textarea", "justificativa");

        const remover = document.createElement("button");
        remover.type = "button";
        remover.className = "btn-remover";
        remover.textContent = "Remover";
        item.appendChild(remover);

        lista.appendChild(item);
        atualizarIndices();
    }

    botaoAdicionar.addEventListener("click", criarAdaptacao);

    lista.addEventListener("click", function (evento) {
        if (!evento.target.classList.contains("btn-remover")) return;

        evento.target.closest(".barreira-item").remove();
        atualizarIndices();
    });

    // Antes de enviar: descarta adaptações totalmente em branco para não gerar erro de validação
    formulario.addEventListener("submit", function () {
        lista.querySelectorAll(".barreira-item").forEach(function (item) {
            const vazio = Array.from(item.querySelectorAll("[data-campo]"))
                .every(function (campo) { return campo.value.trim() === ""; });

            if (vazio) item.remove();
        });

        atualizarIndices();
    });
});
