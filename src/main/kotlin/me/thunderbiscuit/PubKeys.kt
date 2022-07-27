package me.thunderbiscuit

import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.*

class PubKeySB(val x: BigInteger, val y: BigInteger) {

    private val keySpec: ECPublicKeySpec
    val pubKey: PublicKey

    init {
        val parameters: AlgorithmParameters = AlgorithmParameters.getInstance("EC")
        parameters.init(ECGenParameterSpec("secp256k1"))
        val ecParams: ECParameterSpec = parameters.getParameterSpec(ECParameterSpec::class.java)
        val point: ECPoint = ECPoint(x, y)
        keySpec = ECPublicKeySpec(point, ecParams)
        val factory: KeyFactory = KeyFactory.getInstance("EC")
        pubKey = factory.generatePublic(keySpec)
    }

    fun printPubKey() {
        println("Public key from coordinates $x and $y is $pubKey")
    }
}
