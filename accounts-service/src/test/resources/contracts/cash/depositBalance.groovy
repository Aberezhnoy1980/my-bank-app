package contracts.cash

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("cash-service: deposit current user balance via Accounts API")
    request {
        method 'PUT'
        url '/api/accounts/me/balance'
        headers {
            header('Content-Type', 'application/json')
        }
        body([
                operationType: 'DEPOSIT',
                amount       : 100
        ])
    }
    response {
        status 200
        headers {
            header('Content-Type', 'application/json')
        }
        body([
                username : 'demo.user',
                fullName : 'Demo User',
                birthDate: '1995-05-20',
                balance  : 10100.00
        ])
    }
}
