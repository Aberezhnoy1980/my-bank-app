package contracts.transfer

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("transfer-service: fetch account by username for recipient lookup")
    request {
        method 'GET'
        url '/api/accounts/alice.user'
    }
    response {
        status 200
        headers {
            header('Content-Type', 'application/json')
        }
        body([
                username : 'alice.user',
                fullName : 'Alice User',
                birthDate: '1992-03-15',
                balance  : 5000.00
        ])
    }
}
