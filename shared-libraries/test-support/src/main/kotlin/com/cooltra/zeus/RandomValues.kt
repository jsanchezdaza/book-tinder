package com.cooltra.zeus

import java.util.UUID

fun randomIdCardNumber() = "card-number-${UUID.randomUUID()}"

const val DUMMY_PRIVATE_KEY = """-----BEGIN RSA PRIVATE KEY-----
MIIBOAIBAAJAamiMZcYI63j+IPTwR9BQfsLpGFbXzSh0GaX0/dyw+BNBs/YNzgAM
7T8cOdhW503FNK7oh16/wq9S3edYCpQYTwIDAQABAkBHklTE68P6JmLAZh/sHcjW
ymEgluEGRGx7B+5WhoeeA+z5EayF7WI33z5pik7lrwk6BYzHjPzjGsUzuOk3POqx
AiEAwX5NaSLfgcgHQp4vUK5Ruki9IBL2S6f0RENJKxNFJRcCIQCMyGkJQatgZpih
Va/6qPEVnmR86QkQHcabOsDCdiwZiQIgb0Pn08tf50tVEXPRFX4INSzaxHTi+IJu
Lkra6lA8dKUCIByfvuN3661dDtl2v6IaaXI40zumcgZJ15DGQF0Jg+vpAiBTpcUG
fFbfW3gr8dcckrJQ3jDyq6oR4RDzWD/L7GWBPg==
-----END RSA PRIVATE KEY-----"""
