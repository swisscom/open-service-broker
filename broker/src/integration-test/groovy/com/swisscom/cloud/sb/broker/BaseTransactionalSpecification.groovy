package com.swisscom.cloud.sb.broker

import org.springframework.transaction.annotation.Transactional

@Transactional
abstract class BaseTransactionalSpecification extends BaseSpecification {
}
