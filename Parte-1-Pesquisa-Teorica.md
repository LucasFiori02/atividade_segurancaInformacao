# Da Vulnerabilidade ao Deploy Seguro: Shift Left e Shift Right

**Disciplina:** Segurança da Informação  
**Tema:** DevSecOps em pipelines de CI/CD  
**Parte:** Pesquisa teórica individual  
**Data:** 21/09/2026

## Introdução

DevSecOps não é apenas adicionar uma ferramenta de segurança ao final de uma pipeline. A proposta é distribuir responsabilidades e controles de segurança por todo o ciclo de desenvolvimento, mantendo a velocidade de entrega sem tratar segurança como uma inspeção isolada. O [NIST Secure Software Development Framework (SSDF)](https://csrc.nist.gov/pubs/sp/800/218/final) recomenda integrar práticas de desenvolvimento seguro ao modelo de ciclo de vida que a organização já utiliza.

Neste trabalho, "Shift Left" representa a antecipação de atividades de segurança para os momentos em que requisitos, arquitetura, código e dependências ainda podem ser alterados com baixo custo. "Shift Right" representa os controles aplicados depois que o software está implantado, como observabilidade, detecção de ataques, resposta a incidentes e validações controladas em ambientes reais. As duas abordagens são complementares: a primeira reduz a probabilidade de liberar defeitos, enquanto a segunda reduz o tempo de descoberta e o impacto daqueles que escaparem.

## 1. Shift Left e Shift Right

### 1.1 Significado e posição no SDLC

Em uma linha do tempo convencional, o desenvolvimento ocorre aproximadamente nesta ordem:

```text
Requisitos -> Arquitetura -> Codificação -> Pull Request -> Build -> Testes
      |           |            |              |             |        |
      |           |            |              |             |        +-- Shift Left
      |           |            |              |             +----------- Shift Left
      |           |            |              +------------------------- Shift Left
      |           |            +---------------------------------------- Shift Left
      |           +----------------------------------------------------- Shift Left
      +----------------------------------------------------------------- Shift Left

Staging/aceitação -> Deploy -> Produção -> Operação -> Incidente -> Melhoria
       |               |          |           |            |              |
       +-- Left/Right  +-- ponte  +-- Shift Right ------------------------+
```

O desenho é um esquema autoral da distribuição dos controles. Shift Left não significa eliminar as etapas posteriores; significa inserir segurança antes e durante a construção do software. Shift Right começa principalmente no staging e na produção, onde a aplicação encontra configurações, integrações, usuários, volume e padrões de tráfego que não são totalmente reproduzidos no desenvolvimento.

### 1.2 Controles típicos

Três exemplos de Shift Left são:

1. **Threat modeling e revisão de arquitetura:** identificam ativos, fronteiras de confiança, abusos possíveis e requisitos de autenticação, autorização e proteção de dados antes da implementação.
2. **SAST e revisão segura de código:** analisam código-fonte ou bytecode para encontrar padrões como injeção, uso inseguro de APIs e criptografia inadequada antes do merge.
3. **SCA, secret scanning e quality gates:** verificam dependências, histórico do Git, configurações e critérios de aprovação antes que o artefato seja publicado.

Eles são classificados como Shift Left porque fornecem feedback enquanto a mudança ainda está no fluxo de desenvolvimento. O [guia da OWASP sobre análise de código](https://community.owasp.org/Source_Code_Analysis_Tools) observa que ferramentas SAST podem ser integradas ao IDE e à integração contínua, permitindo detectar problemas antes das fases posteriores.

Três exemplos de Shift Right são:

1. **DAST controlado e testes de segurança em staging ou produção:** enviam requisições à aplicação implantada para verificar comportamentos observáveis.
2. **Monitoramento de runtime, WAF/RASP e detecção de anomalias:** identificam tentativas de exploração, abuso de credenciais, padrões incomuns de acesso e alterações no comportamento.
3. **Resposta a incidentes e gestão contínua de vulnerabilidades:** recebem alertas, priorizam riscos, aplicam contenções, fazem correções emergenciais e acompanham novas CVEs depois do deploy.

### 1.3 Abordagens concorrentes ou complementares?

São complementares. Uma pipeline Shift Left robusta pode garantir que o código não contenha uma consulta SQL obviamente insegura, que as dependências conhecidas estejam atualizadas e que os testes automatizados passem. Ainda assim, ela não conhece perfeitamente todas as condições de produção.

Por exemplo, a aplicação pode possuir uma falha de autorização que só ocorre quando dois microsserviços interagem com uma configuração específica de produção. Também pode existir uma vulnerabilidade de SSRF que só é explorável porque, no ambiente real, o servidor consegue acessar o endpoint de metadados da nuvem. A revisão estática pode não modelar esse caminho, e um teste unitário pode não exercitá-lo. DAST, logs de acesso, alertas de comportamento e resposta a incidentes são necessários para detectar ou limitar o problema depois do deploy.

O movimento para a esquerda reduz a quantidade de falhas que chegam à operação; o movimento para a direita reduz o tempo de exposição das falhas restantes. Retirar um deles cria uma falsa sensação de cobertura.

### 1.4 Custo de correção ao longo do ciclo

Em termos relativos, uma falha descoberta durante requisitos ou design costuma ser mais barata de corrigir porque ainda não há muito código, dados migrados, documentação operacional ou consumidores dependentes da decisão. No código e no build, a correção ainda pode ser localizada. Em testes e staging, ela pode exigir retrabalho e nova validação. Em produção, além da mudança técnica, pode envolver rollback, indisponibilidade, investigação forense, comunicação com clientes, notificações regulatórias e dano reputacional.

| Fase | Custo relativo esperado | Exemplos de custo adicional quando a descoberta é tardia |
|---|---:|---|
| Requisitos/design | Muito baixo | Revisão de modelo de ameaça e arquitetura |
| Codificação | Baixo | Alteração de código, testes e revisão |
| Build/integração | Baixo a médio | Falha de pipeline, correção de dependência ou configuração |
| Teste/staging | Médio | Reexecução de testes, reteste de integração e atraso de release |
| Produção | Alto ou muito alto | Contenção, indisponibilidade, investigação, perda de confiança e obrigações legais |

Essa comparação explica o interesse do mercado por Shift Left: feedback precoce tende a ser mais rápido, contextualizado e barato. Entretanto, números absolutos devem ser tratados com cuidado. A chamada “regra dos 100x” é frequentemente repetida como se cada etapa multiplicasse exatamente o custo da anterior, mas não é uma lei universal. O custo depende do tipo de vulnerabilidade, maturidade da equipe, automação, criticidade do sistema, facilidade de rollback, alcance do incidente e qualidade da medição. O [NIST SSDF](https://csrc.nist.gov/pubs/sp/800/218/final) fundamenta a necessidade de prevenir e remediar vulnerabilidades, mas não fixa um multiplicador único de custo. Portanto, a regra pode ser usada como uma metáfora para a tendência de aumento do custo, não como dado preciso aplicável a todo projeto.

## 2. Gestão de segredos

### 2.1 Secret Sprawl e caminhos de vazamento

**Secret Sprawl** é a dispersão descontrolada de segredos por código, arquivos, ferramentas, ambientes e pessoas, sem inventário, proprietário, prazo de validade, controle de acesso ou processo de rotação. Segredos incluem senhas, tokens, chaves de API, chaves privadas, certificados, strings de conexão e credenciais de provedores cloud.

Os caminhos mais comuns de vazamento são:

- commit de uma credencial em um arquivo Java, `.env`, YAML ou arquivo de configuração;
- histórico de commits, mesmo depois de apagar o arquivo em um commit posterior;
- mensagens de log, stack traces, dumps e artefatos de teste;
- imagens Docker, camadas intermediárias, `ENV`, `ARG` ou arquivos copiados para dentro da imagem;
- scripts de CI/CD, variáveis impressas no terminal e artefatos de pipeline;
- pull requests, forks, caches, backups e cópias locais;
- documentação, exemplos e mensagens de suporte que reproduzem o valor real.

A [OWASP Secrets Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html) recomenda centralizar armazenamento, provisionamento, auditoria, rotação e controle de acesso, além de impedir que segredos sejam registrados em texto puro nos logs.

### 2.2 Segredos de CI/build e segredos de runtime

Um **segredo de build/CI** existe para permitir que a pipeline execute uma tarefa de integração ou entrega. Exemplos são o token usado para publicar um pacote, a credencial de leitura de um registry privado ou o token para publicar uma imagem no GHCR. Esse segredo deve estar disponível apenas no job e no momento necessários, com permissões mínimas e, de preferência, curta duração.

Um **segredo de runtime/aplicação** é utilizado pelo processo que atende requisições em execução, como a credencial de um banco de dados, a chave de um gateway de pagamentos ou um certificado usado por um serviço. Ele precisa ser entregue ao ambiente de execução por um mecanismo de configuração seguro, cofre de segredos, identidade de workload ou secret manager. Não deve ser confundido com o `GITHUB_TOKEN`, que é um mecanismo de automação da pipeline e não uma credencial de negócio para a aplicação.

Essa separação limita o raio de impacto. Se um token de CI for comprometido, ele deve permitir somente a ação do pipeline correspondente. Se uma aplicação precisa acessar um banco, ela deve receber uma identidade própria, com permissões apenas sobre os recursos necessários. A documentação do [GitHub Actions sobre uso seguro](https://docs.github.com/en/actions/reference/security/secure-use) recomenda o princípio do menor privilégio para credenciais usadas em workflows.

### 2.3 Por que não colocar segredo de runtime na imagem

Colocar um segredo em `ENV` fixa ou em um arquivo dentro do Dockerfile é uma má prática mesmo em um registry privado por vários motivos:

1. O valor pode ficar gravado em uma camada da imagem e continuar recuperável pelo histórico ou pelo cache.
2. Qualquer pessoa ou processo com acesso à imagem, ao registry, ao host ou ao backup pode obter o segredo.
3. O mesmo valor tende a ser reutilizado entre desenvolvimento, homologação e produção.
4. A rotação exige reconstruir e redistribuir a imagem, em vez de apenas atualizar a configuração do serviço.
5. Logs de build, inspeção da imagem e ferramentas de diagnóstico podem revelar variáveis de ambiente.

A imagem deve ser um artefato reutilizável e sem credenciais específicas de ambiente. O segredo deve ser injetado no deploy, por exemplo por um secret manager, uma identidade federada ou um mecanismo de secrets do orquestrador.

### 2.4 Ferramentas de detecção e de gerenciamento

Ferramentas de **detecção** procuram indícios de segredos já presentes no código, nos arquivos, no histórico ou nos diffs. Elas não são, por si só, um cofre.

- **Gitleaks:** verifica repositórios, arquivos e histórico usando regras para tokens, chaves e padrões de alta entropia. Pode ser executado localmente, como pre-commit hook ou na pipeline. O projeto documenta o uso de fingerprints em `.gitleaksignore` para tratar achados conhecidos e específicos, sem simplesmente desativar toda a detecção.
- **TruffleHog:** examina histórico, branches e conteúdo em busca de padrões de segredos e, quando possível, valida se uma credencial é ativa. A validação aumenta o sinal, mas deve ser feita com cuidado para não causar ações destrutivas nem enviar segredos a serviços externos sem autorização.
- **GitHub Secret Scanning:** serviço integrado ao GitHub que identifica padrões de provedores e padrões personalizados e pode alertar ou bloquear merges em recursos compatíveis.

Ferramentas de **gerenciamento centralizado** armazenam, distribuem, controlam e auditam o uso dos segredos.

- **HashiCorp Vault:** oferece armazenamento protegido, políticas de acesso, auditoria, rotação e credenciais dinâmicas. A documentação do [Vault](https://developer.hashicorp.com/vault/docs/about-vault/how-vault-works) descreve o modelo baseado em identidade, autenticação e autorização.
- **AWS Secrets Manager:** armazena segredos para aplicações e suporta rotação automática ou por função Lambda. A [documentação de rotação da AWS](https://docs.aws.amazon.com/secretsmanager/latest/userguide/rotating-secrets.html) destaca que a credencial deve ser atualizada tanto no secret manager quanto no serviço que a utiliza.
- **GitHub Actions Secrets:** é apropriado para segredos necessários à automação de CI/CD, com escopos de repositório, organização ou ambiente. Não substitui um cofre de runtime quando a aplicação precisa de credenciais em produção.

A diferença central é o objetivo: Gitleaks, TruffleHog e Secret Scanning respondem “onde há um possível segredo exposto?”. Vault, Secrets Manager e Actions Secrets respondem “como guardar, autorizar, entregar, auditar, expirar e rotacionar um segredo que precisa existir?”. Uma organização precisa das duas categorias.

### 2.5 Rotação de segredos

Rotação é substituir periodicamente ou emergencialmente uma credencial por outra, atualizar todos os consumidores e revogar a antiga. Quando um segredo é encontrado exposto, apenas apagar a linha de código não resolve: o valor continua no histórico do Git, em clones, caches, logs ou imagens já publicadas e pode continuar válido.

O procedimento correto é revogar ou desabilitar a credencial, criar uma nova, atualizar o consumidor por um canal seguro, verificar o uso da credencial antiga e investigar os logs. A reescrita do histórico pode reduzir a exposição futura, mas não substitui a revogação, pois cópias antigas podem continuar existindo. A OWASP recomenda rotação, revogação, expiração e auditoria como partes do ciclo de vida do segredo.

## 3. Proteção e qualidade

### 3.1 Branch Protection

**Branch Protection** é o conjunto de regras que restringe alterações diretas em branches importantes, como `main`, e transforma critérios de revisão e automação em pré-requisitos para merge. No GitHub, a documentação de [protected branches](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches) permite configurar, entre outras, as seguintes regras:

1. exigir que mudanças entrem por Pull Request;
2. exigir uma ou mais aprovações de revisores;
3. exigir que os checks de status da pipeline passem;
4. exigir resolução de conversas de revisão;
5. impedir force-push e exclusão da branch;
6. exigir commits assinados, histórico linear ou deploy aprovado.

A regra deve ser calibrada ao fluxo real. Por exemplo, se um job da pipeline faz push automático diretamente na `main`, exigir Pull Request para toda alteração pode fazer esse job falhar. Nesse caso, deve-se ajustar o processo de publicação ou conceder uma exceção cuidadosamente documentada.

### 3.2 Quality Gate

Um **Quality Gate** é uma condição objetiva que determina se uma mudança pode avançar. Exemplos são: zero vulnerabilidades críticas, todos os testes passando, cobertura mínima, nenhum segredo detectado e nenhuma dependência proibida. O gate precisa ter um resultado que bloqueie o próximo estágio ou o merge.

Um relatório apenas informa que foram encontrados problemas; a decisão continua manual e a entrega pode seguir. Um Quality Gate transforma o critério em controle de fluxo. Na pipeline desta atividade, os jobs `secret-scan`, `unit-tests`, `sast`, `sca` e `dockerfile-lint` funcionam como gates porque o job seguinte depende do anterior e a publicação só ocorre quando todos passam.

### 3.3 Testes unitários como controle de segurança

Testes unitários não são uma ferramenta de segurança completa, mas reduzem risco ao verificar invariantes do domínio e contratos de componentes. Um teste pode detectar, por exemplo, cálculo incorreto de autorização, validação de limites, tratamento de entradas inesperadas, aplicação de descontos indevidos, exposição de dados ou comportamento inseguro quando uma dependência falha.

Há relação entre cobertura e superfície de risco, mas não uma equivalência. Cobertura mede quais linhas ou caminhos foram executados; não garante que os casos testados sejam relevantes nem que uma regra de negócio esteja correta. Uma suíte com 100% de cobertura pode não testar autenticação, concorrência, abuso, configuração, integração ou casos de ataque. A prática adequada é combinar cobertura significativa, testes negativos, testes de autorização, testes de integração e análise especializada.

### 3.4 Defesa combinada antes da produção

Branch Protection impede que o fluxo oficial seja contornado. Testes unitários verificam que a mudança preserva contratos funcionais e algumas propriedades de segurança. Quality Gates transformam resultados de scanners e testes em uma condição de passagem. Juntos, eles formam uma primeira linha de defesa:

```text
Desenvolvedor -> Pull Request -> Revisão + Branch Protection
                                  |
                                  v
             Testes + SAST + SCA + Secret Scan + Quality Gates
                                  |
                    somente mudanças aprovadas avançam
```

Ferramentas especializadas ampliam a cobertura, mas não substituem disciplina de revisão, testes e governança. Uma pipeline pode não reconhecer uma regra de negócio nova, enquanto um revisor ou um teste de abuso pode identificá-la. Da mesma forma, uma revisão humana pode não conhecer uma CVE recente, enquanto o SCA pode sinalizá-la.

## 4. SAST, DAST e SCA

### 4.1 SAST

**SAST** (*Static Application Security Testing*) analisa o software sem executar a aplicação como um usuário. Dependendo da ferramenta, a análise pode usar código-fonte, árvore sintática, bytecode ou representação intermediária. Ela procura padrões de fluxo de dados e construções associadas a vulnerabilidades, como uma entrada HTTP chegando a uma consulta SQL por concatenação.

SAST é adequado para IDE, pre-commit, Pull Request e CI. Seu feedback é relativamente cedo e costuma apontar arquivo e linha, mas pode gerar falsos positivos e não compreender perfeitamente configurações externas, infraestrutura, comportamento emergente ou lógica de negócio.

### 4.2 DAST

**DAST** (*Dynamic Application Security Testing*) testa a aplicação em execução, normalmente enviando requisições HTTP e observando respostas, erros, cabeçalhos e comportamentos. Como precisa de um alvo implantado e acessível, é inserido depois do build e do deploy em um ambiente de teste, staging ou produção controlada.

DAST pode encontrar problemas que só aparecem na integração entre aplicação, servidor, banco, proxy e configuração. Por outro lado, tem menos visibilidade do código interno, pode deixar caminhos não exercitados sem cobertura e exige cuidado para não alterar dados reais. Contra produção, deve haver autorização, limites de taxa, escopo restrito, dados seguros e plano de interrupção.

### 4.3 SCA e Log4Shell

**SCA** (*Software Composition Analysis*) inventaria componentes de terceiros e os compara com bases de vulnerabilidades, licenças e versões. Ele é importante porque uma aplicação pode estar vulnerável sem possuir código inseguro escrito pela equipe.

O caso Log4Shell, **CVE-2021-44228**, demonstrou esse risco. A vulnerabilidade afetou o Log4j 2 e envolvia recursos JNDI que podiam ser controlados por entrada do atacante. O [aviso da Apache](https://news.apache.org/foundation/entry/apache-log4j-cves) registra a resposta às vulnerabilidades CVE-2021-44228 e CVE-2021-45046. Uma aplicação poderia ter lógica própria aparentemente correta e ainda assim carregar uma biblioteca vulnerável. SCA detecta a presença e a versão do componente, orientando atualização, remoção ou mitigação.

### 4.4 Comparação

| Técnica | O que analisa | Quando roda | Exemplos | Falhas que pode detectar | Limitações |
|---|---|---|---|---|---|
| SAST | Código-fonte, bytecode ou representação intermediária | IDE, commit, PR e CI, antes do deploy | Semgrep, SonarQube, CodeQL | SQL Injection, XSS em fluxos reconhecíveis, APIs inseguras, segredos e padrões de código | Falsos positivos; pouco contexto de runtime; não garante lógica de negócio |
| DAST | Aplicação implantada e suas respostas | Staging, pré-produção ou produção controlada | OWASP ZAP, Burp Suite, scanners DAST | Falhas de configuração, autenticação, autorização observável, headers, endpoints e comportamento HTTP | Precisa de aplicação funcionando; pode não alcançar caminhos; risco de impacto em dados reais |
| SCA | Dependências diretas/transitivas, imagens e componentes | Build, CI, registry e monitoramento contínuo | Trivy, OWASP Dependency-Check, Dependabot | CVEs conhecidas, versões vulneráveis, licenças incompatíveis e componentes desnecessários | Depende de inventário e bases atualizadas; pode não avaliar exploração real nem código customizado |

As fronteiras entre ferramentas podem se sobrepor: alguns produtos fazem mais de um tipo de análise. A classificação deve considerar o objeto principal e o momento do teste, não apenas o nome comercial da ferramenta.

### 4.5 Relação com Shift Left e Shift Right

SAST é predominantemente Shift Left, pois analisa o artefato antes do deploy. DAST pode atuar nos dois lados: contra staging, é uma validação Shift Left antes de produção; contra produção, de forma autorizada e limitada, é Shift Right.

SCA costuma começar como Shift Left no build, mas não deve terminar ali. Uma nova CVE pode ser publicada depois que a aplicação já foi implantada, como demonstrado pelo Log4Shell. Por isso, é necessário manter inventário de componentes, acompanhar avisos, reavaliar imagens e dependências publicadas e abrir correções mesmo sem mudança funcional no código. Essa combinação também concretiza o processo de remediação contínua recomendado pelo NIST SSDF.

## 5. Infraestrutura e contêineres

### 5.1 Hardening de Dockerfile

Hardening é reduzir a superfície de ataque, a exposição de informações e os privilégios de uma imagem e de seu processo de execução. Más práticas comuns incluem:

1. usar `FROM ...:latest`, pois o conteúdo muda sem revisão e dificulta reproduzir o build;
2. executar o processo como `root` sem necessidade;
3. usar uma imagem base muito grande, com compiladores, shells e utilitários que não são necessários em runtime;
4. usar `ADD` sem necessidade, em vez de `COPY` explícito;
5. copiar senhas, tokens ou arquivos `.env` para a imagem;
6. não usar multi-stage build e levar ferramentas de compilação para a imagem final;
7. instalar pacotes sem fixar versões ou sem remover caches;
8. deixar o contexto de build incluir `.git`, credenciais e arquivos temporários;
9. não verificar a origem e as vulnerabilidades da imagem base.

Hadolint codifica várias dessas recomendações. Por exemplo, a regra `DL3007` alerta sobre `latest`, e `DL3002` alerta quando o último usuário é `root`. O [Hadolint](https://github.com/hadolint/hadolint) faz parsing do Dockerfile e também verifica comandos shell executados em instruções `RUN` por meio do ShellCheck.

### 5.2 Princípio do Menor Privilégio em contêineres

O Princípio do Menor Privilégio estabelece que um processo deve receber somente as permissões necessárias para cumprir sua função. Em um contêiner, isso significa usar usuário não-root, filesystem somente leitura quando possível, capabilities mínimas, ausência de dispositivos desnecessários e acesso restrito à rede, volumes e secrets.

Um processo rodando como `root` dentro do contêiner representa risco porque uma falha na aplicação pode permitir alteração de arquivos, execução de ferramentas, coleta de credenciais ou exploração de uma vulnerabilidade do runtime. O isolamento de contêiner não é uma fronteira absoluta: uma cadeia de escape, uma configuração fraca do daemon ou um volume privilegiado pode ampliar o impacto para o host. Mesmo que a lógica de negócio tenha sido escrita pensando em root, a necessidade deve ser questionada e, quando possível, separada em uma etapa privilegiada mínima.

### 5.3 Multi-stage build

Um **multi-stage build** usa várias instruções `FROM`, normalmente uma etapa de compilação com JDK e ferramentas de build e uma etapa final com apenas o JRE e o artefato necessário. O resultado da primeira etapa é copiado para a segunda com `COPY --from=...`.

Essa separação evita que Maven, compiladores, fontes, caches e arquivos temporários cheguem à imagem final. A imagem fica menor, é transferida mais rapidamente e possui menos componentes que poderiam ser explorados. A documentação do [Docker sobre multi-stage builds](https://docs.docker.com/get-started/docker-concepts/building-images/multi-stage-builds/) recomenda essa separação justamente para reduzir tamanho e superfície de ataque. Ela não transforma automaticamente a imagem em segura: a base final ainda precisa ser atualizada, executada sem root e escaneada.

### 5.4 Linting e escaneamento de imagens

**Hadolint** é um linter de Dockerfile. Ele verifica estilo, ordem e práticas inseguras antes de a imagem ser construída. É útil no editor e como gate da pipeline, mas analisa a receita; não sabe, sozinho, todas as vulnerabilidades introduzidas pela imagem base ou pelas bibliotecas instaladas.

**Trivy** escaneia sistemas de arquivos, dependências, imagens, configurações e outros alvos. No modo filesystem, pode procurar vulnerabilidades em manifests e lockfiles; no modo image, pode verificar pacotes do sistema, bibliotecas da aplicação, segredos e configurações conforme os scanners habilitados. A [documentação do Trivy](https://trivy.dev/docs/dev/target/filesystem/) explica o uso de `trivy fs` para projetos locais e pipelines. O [Docker recomenda](https://docs.docker.com/build/building/best-practices/) combinar imagens adequadas, multi-stage builds, usuário não-root e controle de secrets.

Hadolint e Trivy têm funções diferentes e complementares: o primeiro avalia como a imagem é construída; o segundo avalia o conteúdo resultante e suas dependências conhecidas.

## Conclusão

Shift Left e Shift Right devem ser tratados como um ciclo. O Shift Left cria barreiras antes do merge e reduz custo de correção por meio de threat modeling, revisão, testes, secret scanning, SAST, SCA e quality gates. O Shift Right reconhece que nenhum controle preventivo é perfeito e acrescenta DAST controlado, observabilidade, gestão de vulnerabilidades, resposta a incidentes, rotação de credenciais e monitoramento de novas ameaças.

No cenário da BancoFácil Digital, a pipeline proposta materializa a primeira parte desse ciclo: ela bloqueia segredos, testes quebrados, padrões inseguros, dependências vulneráveis e Dockerfiles frágeis antes da publicação. Para completar a estratégia, a empresa ainda precisaria monitorar a aplicação publicada, verificar continuamente seus componentes, manter uma resposta operacional a incidentes, proteger a branch principal, controlar privilégios e testar o comportamento do sistema em ambientes implantados.

## Referências

- APACHE SOFTWARE FOUNDATION. [Apache Log4j security advisories](https://news.apache.org/foundation/entry/apache-log4j-cves). Acesso em 21 set. 2026.
- AWS. [Rotate AWS Secrets Manager secrets](https://docs.aws.amazon.com/secretsmanager/latest/userguide/rotating-secrets.html). Acesso em 21 set. 2026.
- DOCKER. [Building best practices](https://docs.docker.com/build/building/best-practices/). Acesso em 21 set. 2026.
- DOCKER. [Multi-stage builds](https://docs.docker.com/get-started/docker-concepts/building-images/multi-stage-builds/). Acesso em 21 set. 2026.
- GITHUB. [Managing protected branches](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches). Acesso em 21 set. 2026.
- GITHUB. [Secrets reference for GitHub Actions](https://docs.github.com/en/actions/reference/security/secrets). Acesso em 21 set. 2026.
- GITHUB. [Secure use reference for GitHub Actions](https://docs.github.com/en/actions/reference/security/secure-use). Acesso em 21 set. 2026.
- GITLEAKS. [Find secrets with Gitleaks](https://github.com/gitleaks/gitleaks). Acesso em 21 set. 2026.
- HADOLINT. [Haskell Dockerfile Linter](https://github.com/hadolint/hadolint). Acesso em 21 set. 2026.
- HASHICORP. [How Vault works](https://developer.hashicorp.com/vault/docs/about-vault/how-vault-works). Acesso em 21 set. 2026.
- NIST. SOUPPAYA, M.; SCARFONE, K.; DODSON, D. [SP 800-218: Secure Software Development Framework (SSDF) Version 1.1](https://csrc.nist.gov/pubs/sp/800/218/final). Gaithersburg: NIST, 2022. Acesso em 21 set. 2026.
- OWASP. [Secrets Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html). Acesso em 21 set. 2026.
- OWASP. [Source Code Analysis Tools](https://community.owasp.org/Source_Code_Analysis_Tools). Acesso em 21 set. 2026.
- TRIVY. [Filesystem scanning](https://trivy.dev/docs/dev/target/filesystem/). Acesso em 21 set. 2026.
- TRUFFLE SECURITY. [TruffleHog](https://github.com/trufflesecurity/trufflehog). Acesso em 21 set. 2026.

