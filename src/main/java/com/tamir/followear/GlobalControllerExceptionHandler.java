package com.tamir.followear;

import com.tamir.followear.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserCollisionException.class)
    @ResponseBody
    public ErrorMessage handleUserCollisionException(UserCollisionException e){
        ErrorMessage error = new ErrorMessage("User Collision", e.getMessage());
        return error;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidUserException.class)
    @ResponseBody
    public ErrorMessage handleInvalidUserException(InvalidUserException e){
        ErrorMessage error = new ErrorMessage("Invalid User", "The user does not exist");
        return error;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidPostException.class)
    @ResponseBody
    public ErrorMessage handleInvalidPostException(InvalidPostException e) {
        ErrorMessage error = new ErrorMessage("Invalid Post", "The post does not exist");
        return error;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(S3Exception.class)
    @ResponseBody
    public ErrorMessage handleS3Exception(S3Exception e){
        ErrorMessage error = new ErrorMessage("S3 Error", e.getMessage());
        return error;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(StreamException.class)
    @ResponseBody
    public ErrorMessage handleStreamException(StreamException e){
        ErrorMessage error = new ErrorMessage("Stream Error", e.getMessage());
        return error;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ExceptionHandler(NoMoreActivitiesException.class)
    @ResponseBody
    public ErrorMessage handleNoMoreActivitiesException(NoMoreActivitiesException e){
        ErrorMessage error = new ErrorMessage("No More Feed Activities",
                "There are no more activities in this feed");
        return error;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidAuthData.class)
    @ResponseBody
    public ErrorMessage handleInvalidAuthData(InvalidAuthData e){
        ErrorMessage error = new ErrorMessage("Invalid Authentication Data", e.getMessage());
        return error;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidPassword.class)
    @ResponseBody
    public ErrorMessage handleInvalidPassowrd(InvalidPassword e){
        ErrorMessage error = new ErrorMessage("Invalid Password", e.getMessage());
        return error;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidToken.class)
    @ResponseBody
    public ErrorMessage handleInvalidToken(InvalidToken e) {
        ErrorMessage error = new ErrorMessage("Invaid Token", e.getMessage());
        return error;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(UrlException.class)
    @ResponseBody
    public ErrorMessage handleUrlException(UrlException e) {
        ErrorMessage error = new ErrorMessage("Malformed URL", e.getMessage());
        return error;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NoAuthException.class)
    @ResponseBody
    public ErrorMessage handleNoAuthException(NoAuthException e) {
        ErrorMessage error = new ErrorMessage("Not Authorized", e.getMessage());
        return error;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(CsrfException.class)
    @ResponseBody
    public ErrorMessage handleCsrfException(CsrfException e) {
        ErrorMessage error = new ErrorMessage("Csrf Exception", e.getMessage());
        return error;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(CognitoException.class)
    @ResponseBody
    public ErrorMessage handleCognitoException(CognitoException e) {
        ErrorMessage error = new ErrorMessage("Cognito Exception", e.getMessage());
        return error;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidCode.class)
    @ResponseBody
    public ErrorMessage handleInvalidCode(InvalidCode e) {
        ErrorMessage error = new ErrorMessage("Invalid Confirmation Code", e.getMessage());
        return error;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ExchangeRateException.class)
    @ResponseBody
    public ErrorMessage handleExchangeRateException(ExchangeRateException e) {
        ErrorMessage error = new ErrorMessage("Exchange Rate Exception", e.getMessage());
        return error;
    }
}
