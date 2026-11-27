/* Busca de relatórios na listagem */
const campoBuscaRelatorios = document.getElementById("busca-relatorios");
const cardsRelatorios = document.querySelectorAll(".relatorio-card");
const mensagemSemResultados = document.getElementById("relatorios-sem-resultados");

function normalizarTexto(texto) {
    return (texto || "")
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "");
}

if (campoBuscaRelatorios && mensagemSemResultados) {
    campoBuscaRelatorios.addEventListener("input", function () {
        const termo = normalizarTexto(campoBuscaRelatorios.value);
        let encontrados = 0;

        cardsRelatorios.forEach(function (card) {
            const corresponde = normalizarTexto(card.textContent).includes(termo);
            card.hidden = !corresponde;
            if (corresponde) encontrados++;
        });

        mensagemSemResultados.hidden = encontrados > 0;
    });
}

/* Vínculo e cadastro integrado de aluno */
document.addEventListener("DOMContentLoaded", function () {
    const area = document.getElementById("area-vinculo-aluno");
    if (!area) return;

    let alunos = window.alunosDisponiveis || [];

    const painelCadastrado = document.getElementById("painel-aluno-cadastrado");
    const painelNovo = document.getElementById("painel-aluno-novo");
    const busca = document.getElementById("busca-aluno-relatorio");
    const sugestoes = document.getElementById("sugestoes-alunos-relatorio");
    const alunoId = document.getElementById("aluno-id-relatorio");
    const selecionado = document.getElementById("aluno-selecionado-relatorio");
    const botaoSalvar = document.getElementById("salvar-aluno-integrado");
    const mensagens = document.getElementById("erros-cadastro-aluno");
    const formulario = area.closest("form");

    function mostrarAluno(aluno) {
        document.getElementById("aluno-nome-relatorio").textContent = aluno.nome || "";
        document.getElementById("aluno-cpf-relatorio").textContent =
            "CPF: " + (aluno.cpf || "Não informado");
        document.getElementById("aluno-matricula-relatorio").textContent =
            "Matrícula: " + (aluno.matricula?.numeroMatricula || "Não informada");
        document.getElementById("aluno-curso-relatorio").textContent =
            "Curso: " + (aluno.matricula?.curso || "Não informado");
        document.getElementById("aluno-ingresso-relatorio").textContent =
            "Ingresso: " + (aluno.matricula?.periodoIngresso || "Não informado");
        document.getElementById("aluno-periodo-relatorio").textContent =
            "Período letivo: " + (aluno.matricula?.periodoLetivo || "Não informado");

        selecionado.hidden = false;
    }

    function selecionarAluno(aluno) {
        alunoId.value = aluno.id;
        busca.value = aluno.nome || "";
        mostrarAluno(aluno);
        sugestoes.innerHTML = "";
        sugestoes.classList.remove("ativa");
    }

    function atualizarPainel() {
        const tipo = area.querySelector(
            'input[name="tipoVinculoAluno"]:checked'
        )?.value;

        const modoNovo = tipo === "novo";

        painelCadastrado.hidden = modoNovo;
        painelNovo.hidden = !modoNovo;
        mensagens.textContent = "";
        mensagens.classList.remove("sucesso-cadastro-aluno");

        if (modoNovo) {
            alunoId.value = "";
            selecionado.hidden = true;
            sugestoes.classList.remove("ativa");
        }
    }

    area.querySelectorAll('input[name="tipoVinculoAluno"]').forEach(radio => {
        radio.addEventListener("change", atualizarPainel);
    });

    busca.addEventListener("input", function () {
        const termo = normalizarTexto(busca.value.trim());

        alunoId.value = "";
        selecionado.hidden = true;
        sugestoes.innerHTML = "";

        if (!termo) {
            sugestoes.classList.remove("ativa");
            return;
        }

        const encontrados = alunos.filter(aluno => {
            const nome = normalizarTexto(aluno.nome);
            const matricula = normalizarTexto(
                aluno.matricula?.numeroMatricula
            );

            return nome.includes(termo) || matricula.includes(termo);
        });

        encontrados.forEach(aluno => {
            const item = document.createElement("button");
            item.type = "button";
            item.className = "sugestao-aluno";

            const nome = document.createElement("strong");
            nome.textContent = aluno.nome || "Nome não informado";

            const matricula = document.createElement("span");
            matricula.textContent =
                "Matrícula: " +
                (aluno.matricula?.numeroMatricula || "Não informada");

            const curso = document.createElement("span");
            curso.textContent =
                "Curso: " + (aluno.matricula?.curso || "Não informado");

            item.append(nome, matricula, curso);
            item.addEventListener("click", () => selecionarAluno(aluno));
            sugestoes.appendChild(item);
        });

        if (encontrados.length === 0) {
            const vazio = document.createElement("p");
            vazio.textContent = "Nenhum aluno encontrado.";
            sugestoes.appendChild(vazio);
        }

        sugestoes.classList.add("ativa");
    });

    botaoSalvar.addEventListener("click", async function () {
        mensagens.textContent = "";
        mensagens.classList.remove("sucesso-cadastro-aluno");
        botaoSalvar.disabled = true;
        botaoSalvar.textContent = "Salvando aluno...";

        try {
            const dados = new URLSearchParams();

            area.querySelectorAll("#painel-aluno-novo [name]").forEach(campo => {
                dados.append(campo.name, campo.value);
            });

            const csrf = formulario.querySelector("#csrf-token");
            if (csrf && csrf.name) {
                dados.append(csrf.name, csrf.value);
            }

            const resposta = await fetch("/alunos/cadastro-integrado", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                    "Accept": "application/json"
                },
                body: dados.toString(),
                credentials: "same-origin"
            });

            const resultado = await resposta.json();

            if (!resposta.ok) {
                if (resultado.erros?.length) {
                    mensagens.textContent = resultado.erros
                        .map(erro => erro.campo + ": " + erro.mensagem)
                        .join(" | ");
                } else {
                    mensagens.textContent =
                        resultado.mensagem || "Não foi possível cadastrar o aluno.";
                }
                return;
            }

            const novoAluno = {
                id: resultado.id,
                nome: resultado.nome,
                cpf: resultado.cpf,
                matricula: {
                    numeroMatricula: resultado.matricula,
                    curso: resultado.curso,
                    periodoIngresso: resultado.periodoIngresso,
                    periodoLetivo: resultado.periodoLetivo
                }
            };

            alunos.push(novoAluno);

            area.querySelector(
                'input[name="tipoVinculoAluno"][value="cadastrado"]'
            ).checked = true;

            atualizarPainel();
            selecionarAluno(novoAluno);

            mensagens.textContent = "Aluno cadastrado e vinculado ao relatório.";
            mensagens.classList.add("sucesso-cadastro-aluno");

        } catch (erro) {
            mensagens.textContent =
                "Erro de comunicação ao cadastrar o aluno. Tente novamente.";
        } finally {
            botaoSalvar.disabled = false;
            botaoSalvar.textContent = "Salvar aluno";
        }
    });

    formulario.addEventListener("submit", function (evento) {
        if (!alunoId.value) {
            evento.preventDefault();
            mensagens.textContent =
                "Selecione um aluno cadastrado ou cadastre o novo aluno antes de salvar o relatório.";

            if (painelNovo.hidden) {
                busca.focus();
            }
        }
    });
});
