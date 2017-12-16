package com.VO


class MailVO {

    String from
    String to
    String text
    String subject
    String html

    MailVO()
    {

    }
    MailVO(String from,String to,String text,String subject,String html)
    {
        this.from=from
        this.to=to
        this.text=text
        this.subject=subject
        this.html=html
    }
}
