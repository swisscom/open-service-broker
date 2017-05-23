package com.swisscom.cf.broker

import org.springframework.transaction.annotation.Transactional

@Transactional
abstract class BaseTransactionalSpecification extends BaseSpecification {
}
