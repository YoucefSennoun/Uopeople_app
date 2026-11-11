package edu.uopeople.app.web

object DomainRules {

    val EXTERNAL_SCHEMES = setOf("mailto", "tel", "sms", "geo", "market", "intent")

    private val ALLOWED_DOMAINS = listOf(
        "uopeople.edu",
        "learning.uopeople.edu",
        "brightspace.com",
        "d2l.com",
        "my.uopeople.edu"
    )

    fun isInternalHost(host: String?): Boolean {
        if (host.isNullOrBlank()) return true
        val normalized = host.lowercase().trim().trimEnd('.')
        return ALLOWED_DOMAINS.any { domain ->
            normalized == domain || normalized.endsWith(".$domain")
        }
    }
}
