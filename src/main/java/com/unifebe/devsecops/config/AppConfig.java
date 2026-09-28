package com.unifebe.devsecops.config;

/**
 * Configuracao sensivel fornecida pelo ambiente de execucao.
 *
 * Em producao, essas variaveis devem ser populadas por um cofre de segredos
 * como Vault ou AWS Secrets Manager. O GITHUB_TOKEN pertence a CI/build e nao
 * deve ser reutilizado como segredo de runtime da aplicacao.
 */
public final class AppConfig {

    private AppConfig() {
    }

    public static String dbPassword() {
        return System.getenv("DB_PASSWORD");
    }

    public static String awsAccessKeyId() {
        return System.getenv("AWS_ACCESS_KEY_ID");
    }

    public static String awsSecretAccessKey() {
        return System.getenv("AWS_SECRET_ACCESS_KEY");
    }

    public static String paymentGatewayApiKey() {
        return System.getenv("PAYMENT_GATEWAY_API_KEY");
    }
}
