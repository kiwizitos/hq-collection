package com.kiwizitos.collection.data.remote

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class GuiaAccessChallengeException : IOException(
    "O Guia dos Quadrinhos exige uma verificação de acesso no navegador."
)

class GuiaUnexpectedPageException : IOException(
    "O Guia dos Quadrinhos retornou uma página com estrutura inesperada."
)

object GuiaHtml {
    fun document(html: String): Document {
        val doc = Jsoup.parse(html)
        if (doc.selectFirst("script[src*='/cdn-cgi/challenge-platform/'], #challenge-form") != null ||
            html.contains("window._cf_chl_opt")) {
            throw GuiaAccessChallengeException()
        }
        return doc
    }
}

object GuiaFailureMessages {
    fun message(error: Throwable): String = when (error) {
        is GuiaAccessChallengeException ->
            "O Guia dos Quadrinhos exige verificação no navegador e bloqueou o acesso do app. " +
                "Seus itens salvos foram preservados. Tente acessar o site no navegador."
        is GuiaUnexpectedPageException ->
            "Não foi possível reconhecer a página do Guia dos Quadrinhos. " +
                "O site pode ter mudado ou estar indisponível."
        is HttpException -> when (error.code()) {
            403 -> "O Guia dos Quadrinhos negou o acesso (403). Seus itens salvos foram preservados."
            404 -> "Esta página não foi encontrada no Guia dos Quadrinhos (404)."
            429 -> "O Guia dos Quadrinhos limitou as consultas. Aguarde antes de tentar novamente."
            in 500..599 -> "O Guia dos Quadrinhos está temporariamente indisponível (${error.code()})."
            else -> "Falha ao consultar o Guia dos Quadrinhos (HTTP ${error.code()})."
        }
        is UnknownHostException -> "Sem conexão com a internet."
        is SocketTimeoutException -> "Tempo de conexão esgotado. Tente novamente."
        is IOException -> "Erro de conexão. Verifique sua internet."
        is IllegalArgumentException -> error.message ?: "Parâmetros inválidos."
        else -> "Não foi possível concluir a consulta. Tente novamente."
    }
}
