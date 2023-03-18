package com.occ.annotation

@Retention
@Target(allowedTargets = [AnnotationTarget.FIELD])
annotation class CoreDecryption(val keyName: String)
