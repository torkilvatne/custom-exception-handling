package errorHandling

class CustomException : RuntimeException {
    constructor(error: CustomError): super(error.code + " : " + error.errorMsg) {}
    constructor(error: CustomError, cause: Throwable): super(error.code + " : " + error.errorMsg, cause) {}
}