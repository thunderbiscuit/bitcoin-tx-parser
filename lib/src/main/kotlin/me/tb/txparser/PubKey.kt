/*
 * Copyright 2022-2024 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package me.tb.txparser

import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec

// class PubKey(val x: BigInteger, val y: BigInteger) {
//
//     private val keySpec: ECPublicKeySpec
//     val pubKey: PublicKey
//
//     init {
//         val parameters: AlgorithmParameters = AlgorithmParameters.getInstance("EC")
//         parameters.init(ECGenParameterSpec("secp256k1"))
//         val ecParams: ECParameterSpec = parameters.getParameterSpec(ECParameterSpec::class.java)
//         val point: ECPoint = ECPoint(x, y)
//         keySpec = ECPublicKeySpec(point, ecParams)
//         val factory: KeyFactory = KeyFactory.getInstance("EC")
//         pubKey = factory.generatePublic(keySpec)
//     }
//
//     fun printPubKey() {
//         println("Public key from coordinates $x and $y is $pubKey")
//     }
// }
