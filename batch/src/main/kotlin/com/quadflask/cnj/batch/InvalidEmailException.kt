package com.quadflask.cnj.batch

class InvalidEmailException(email: String?) : Exception("the email $email isn't valid")