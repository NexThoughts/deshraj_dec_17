package com.verticles

import com.VO.MailVO
import io.vertx.core.Vertx
import io.vertx.ext.mail.MailClient


class MailService {


    public void sendMail(MailVO mailVO)
    {
        Vertx vertx= Vertx.vertx()
        Map<String,String> config = [:]
        config.hostname = "smtp.gmail.com"
        config.port = 587
        config.starttls = "OPTIONAL"
        config.username = ""
        config.password = ""

        println "---sendEmail--2--------" + config

        MailClient mailClient = MailClient.createNonShared(vertx, config)

        println "-----mailClient-------" + mailClient

        Map message = [:]
        message.from = "${mailVO?.from}"
        message.to = "${mailVO?.to}"
//        message.cc = "${mailVO?.cc}"
        message.text = "${mailVO?.text}"
        message.subject = "${mailVO?.subject}"
        message.html = "${mailVO?.html}"

        println "-----message-------" + message

        mailClient.sendMail(message, { result ->
            println "--------- Sending Email Message ---------"
            if (result.succeeded()) {
                println(result.result())
                println "--------- Sending Email Message Succeeded---------"
            } else {
                println "--------- Sending Email Message FAILED---------"
                result.cause().printStackTrace()
            }
        })
    }

}


