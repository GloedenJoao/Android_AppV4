# Finance Planner (Android)

App Android em Jetpack Compose para simular e acompanhar saldos diários de contas, caixinhas CDB, vales e cartão de crédito. Usa tema escuro, navegação por abas inferiores e telas para inputs, simulações e dashboard.

## Recursos principais
- **Tela inicial:** cards com saldo da conta corrente, total em caixinhas, vales e dívida do cartão; tabela diária colorida por sinal.
- **Inputs:** formulários específicos para conta corrente, caixinhas CDB (adicionar/editar/remover), vales (ajuste de dia de crédito ou penúltimo dia útil), salário com ajuste automático para dia útil e configuração do cartão.
- **Simulação:** cadastro de transações únicas ou por intervalos de datas, escolha de débito/crédito, origem e destino para transferências; listas de eventos padrão e simulados com exclusão individual ou total.
- **Dashboard:** filtros de período, visão de variação início vs fim e gráficos simples de faixa diária e linha para totais.

## Como executar
1. Abra o projeto no Android Studio Iguana ou mais recente.
2. Sincronize os gradle scripts (JDK 17, compileSdk 34).
3. Rode o app em um dispositivo/emulador Android 8.0+.

## Dicas de UX
- Todos os textos estão em português e o tema escuro está ativado por padrão.
- Use os chips para alternar tipo, origem e destino nas simulações.
- Os campos de data aceitam formatos `yyyy-MM-dd` e intervalos com `..` separados por `;` (ex.: `2024-12-13;2024-12-15..2024-12-20`).

## Manutenção
- Mantém este README e o `AGENTS.md` alinhados a novas telas, fluxos ou dependências.
- Documente mudanças de regras de negócio (como cálculo do penúltimo dia útil ou ajustes de fim de semana) sempre que alterá-las.
