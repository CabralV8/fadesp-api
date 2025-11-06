package com.fadesp.payment.util;

import java.util.regex.Pattern;

public class IsCpfCnpj {
    private static final Pattern SOMENTE_DIGITOS = Pattern.compile("\\D+");

    private IsCpfCnpj() {
        throw new UnsupportedOperationException("Classe utilitária - não deve ser instanciada.");
    }


    private static String limpar(String valor) {
        if (valor == null) return "";
        return SOMENTE_DIGITOS.matcher(valor.trim()).replaceAll("");
    }


    public static boolean isCpfOrCnpj(String documento) {
        String numeros = limpar(documento);
        if (numeros.length() == 11) {
            return isCpf(numeros);
        } else if (numeros.length() == 14) {
            return isCnpj(numeros);
        }
        return false;
    }

    public static boolean isCpf(String cpf) {
        String numeros = limpar(cpf);
        if (numeros.length() != 11 || todosDigitosIguais(numeros)) return false;

        int digito1 = calcularDigitoCpf(numeros, 10);
        int digito2 = calcularDigitoCpf(numeros, 11);
        return digito1 == (numeros.charAt(9) - '0') && digito2 == (numeros.charAt(10) - '0');
    }

    private static int calcularDigitoCpf(String numeros, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < pesoInicial - 1; i++) {
            soma += (numeros.charAt(i) - '0') * (pesoInicial - i);
        }
        int resto = soma % 11;
        return (resto < 2) ? 0 : 11 - resto;
    }

    public static boolean isCnpj(String cnpj) {
        String numeros = limpar(cnpj);
        if (numeros.length() != 14 || todosDigitosIguais(numeros)) return false;

        int digito1 = calcularDigitoCnpj(numeros, 12);
        int digito2 = calcularDigitoCnpj(numeros, 13);
        return digito1 == (numeros.charAt(12) - '0') && digito2 == (numeros.charAt(13) - '0');
    }

    private static int calcularDigitoCnpj(String numeros, int posicaoFinal) {
        int soma = 0;
        int peso = (posicaoFinal == 12) ? 5 : 6;

        for (int i = 0; i < posicaoFinal; i++) {
            soma += (numeros.charAt(i) - '0') * peso;
            peso = (peso == 2) ? 9 : peso - 1;
        }

        int resto = soma % 11;
        return (resto < 2) ? 0 : 11 - resto;
    }


    private static boolean todosDigitosIguais(String valor) {
        char primeiro = valor.charAt(0);
        for (int i = 1; i < valor.length(); i++) {
            if (valor.charAt(i) != primeiro) {
                return false;
            }
        }
        return true;
    }
}

