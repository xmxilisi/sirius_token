package com.contract.modules.contract.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> logger(from: T): Logger {
    return LoggerFactory.getLogger(T::class.java)
}

inline fun <reified T> log(from: T): Logger = LoggerFactory.getLogger(T::class.java)