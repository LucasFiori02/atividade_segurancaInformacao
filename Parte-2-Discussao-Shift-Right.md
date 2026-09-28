# Discussão Final: Shift Left e Shift Right

## Controles implementados

O piloto da BancoFácil implementa principalmente controles de **Shift Left**. O secret scanning com Gitleaks impede que credenciais sejam introduzidas ou permaneçam sem tratamento no repositório. Os testes unitários verificam regras funcionais antes do avanço do artefato. O Semgrep faz análise estática do código-fonte e identifica padrões como SQL Injection. O Trivy verifica dependências e vulnerabilidades conhecidas, enquanto o Hadolint avalia práticas inseguras no Dockerfile. A proteção da branch `main`, quando configurada, transforma esses resultados em uma condição formal para o merge.

Esses controles são executados antes da publicação da imagem. A pipeline usa dependências entre jobs para impedir que o build e o push para o GHCR ocorram quando algum gate de segurança falha. Isso reduz o custo de correção e evita que um artefato conhecido como vulnerável avance para o próximo estágio.

## Extensões para Shift Right

Alguns controles podem ser estendidos para depois do deploy. O Trivy pode reavaliar periodicamente imagens e dependências já publicadas, pois uma nova CVE pode ser divulgada depois da construção da imagem. Um scanner DAST, como o OWASP ZAP, pode testar a aplicação implantada em staging e, de forma controlada, endpoints selecionados em produção. Logs, métricas e alertas podem identificar tentativas de exploração, padrões anômalos de acesso e abuso de credenciais.

O caso do Log4j mostra por que a atividade não termina quando a pipeline fica verde: uma vulnerabilidade nova pode surgir em uma versão que era considerada aceitável no momento do deploy. A resposta operacional deve incluir inventário de componentes, alertas de vulnerabilidades, atualização emergencial, rotação de segredos e um plano de contenção.

Feature flags e liberações graduais também conectam os dois lados. Uma correção pode ser publicada para uma pequena parcela do tráfego, observada e expandida progressivamente. Se métricas ou alertas indicarem regressão, a mudança pode ser desativada sem rollback amplo.

## O que ainda falta à BancoFácil

Para fechar o ciclo completo, a organização ainda precisa de:

- DAST automatizado contra staging e testes controlados de produção;
- observabilidade centralizada com logs estruturados, métricas e rastreamento;
- alertas para exploração, anomalias e uso de credenciais revogadas;
- inventário contínuo e SBOM das imagens e dependências;
- gestão de incidentes, comunicação e exercícios de resposta;
- cofre de segredos com rotação automática e identidades de workload;
- atualização contínua de imagens, bibliotecas e regras de detecção;
- revisão periódica dos Quality Gates conforme o risco do negócio.

Assim, Shift Left reduz a probabilidade de liberar uma vulnerabilidade, enquanto Shift Right reduz o tempo de descoberta, contenção e recuperação quando uma falha passa pelos controles preventivos. A segurança efetiva depende da combinação dos dois movimentos.
