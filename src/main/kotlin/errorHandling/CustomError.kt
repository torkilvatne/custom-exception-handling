package errorHandling

import javax.ws.rs.core.Response.Status
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

data class CustomError(
    val code: String,
    val errorMsg: String,
    val status: Int,
    val context: Map<String, String>,
    val loggedContext: Map<String, String>,
    val cause: Throwable?
) {

    val GENERAL_CUSTOMERROR: CustomError = createError(
        "ERROR",
        "Unknown error"
    )

    val VALUE_NOT_HIGHER_THAN_FIVE: CustomError =
        createError(
            "INVALID_INPUT_001",
            "Value added can't be higher than five"
        )

    val COLOUR_MOOD_MISMATCH: CustomError =
        createError(
            "INVALID_STATE",
            "You can not have blue as favourite color and be sad"
        )

    val CANNOT_BE_PROCESSED: CustomError = createError(
        "INTERNAL_ERROR_001",
        "Object cannot be processed"
    ).withStatus(INTERNAL_SERVER_ERROR)

    private fun createError(
        code: String,
        errorMsg: String
    ): CustomError {
        return CustomError(
            code,
            errorMsg,
            BAD_REQUEST.statusCode,
            mapOf<String, String>(),
            mapOf(),
            Throwable()
        )
    }

    fun withStatus(status: Status): CustomError {
        return withStatus(status.statusCode)
    }

    fun withStatus(status: Int): CustomError {
        return if (this.status == status) this else CustomError(
            this.code,
            this.errorMsg,
            status,
            this.context,
            this.loggedContext,
            cause
        )
    }

    fun withAdditionalInfo(value: String): CustomError {
        return withContext("Additional information", value)
    }

    fun withContext(
        key: String,
        value: Any
    ): CustomError {
        val newKontekst: HashMap<String, String> = HashMap<String, String>(context)
        newKontekst[key] = value.toString()
        return CustomError(
            this.code,
            this.errorMsg,
            status,
            newKontekst,
            this.loggedContext,
            cause
        )
    }

    fun withLoggedContext(
        key: String,
        value: Any
    ): CustomError {
        val newLoggetKontekst: HashMap<String, String> = HashMap<String, String>(loggedContext)
        newLoggetKontekst[key] = value.toString()
        return CustomError(
            this.code,
            this.errorMsg,
            status,
            this.context,
            newLoggetKontekst,
            cause
        )
    }

    private fun withCause(cause: Throwable): CustomError {
        return CustomError(
            this.code,
            this.errorMsg,
            status,
            this.context,
            this.loggedContext,
            cause
        )
    }

    fun forException(e: Throwable): CustomError {
        val feil: CustomError = withCause(e)
            .withLoggedContext(
                "exceptionMessage",
                e.message ?: "Something went wrong fetching the logged context for the thrown error."
            )
            .withLoggedContext("exceptionClass", e.javaClass.simpleName)
        return if (e.cause != null) {
            feil
                .withLoggedContext(
                    "exceptionCauseMessage",
                    e.cause!!.message ?: "Something went wrong fetching the logged context for the thrown error."
                )
                .withLoggedContext("exceptionCauseClass", e.cause!!.javaClass.simpleName)
        } else feil
    }

    fun throwException() {
        throw CustomException(this)
    }

    fun throwException(cause: Throwable) {
        throw CustomException(this, cause)
    }

    fun toException(): CustomException {
        return if (cause != null) CustomException(this, cause) else CustomException(this)
    }

    fun toException(cause: Throwable?): CustomException? {
        return forException(cause!!).toException()
    }
}
