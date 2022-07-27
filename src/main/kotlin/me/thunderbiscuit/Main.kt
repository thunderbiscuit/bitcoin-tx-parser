package me.thunderbiscuit

fun main() {
    // val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance("EC")
    // val ecSpec: ECGenParameterSpec = ECGenParameterSpec("secp256k1")
    // keyPairGenerator.initialize(ecSpec, SecureRandom())
    //
    // val keyPair: KeyPair = keyPairGenerator.generateKeyPair()
    // val publicKey: PublicKey = keyPair.public
    // val privateKey: PrivateKey = keyPair.private
    //
    // println("Public key: $publicKey")
    // println("Public key: ${publicKey.format}")
    // println("Private key: $privateKey")
    //
    // // sign a message
    // val ecdsaSign: Signature = Signature.getInstance("NONEwithECDSA")
    // // val ecdsaSign: Signature = Signature.getInstance("SHA256withECDSA")
    // ecdsaSign.initSign(privateKey)
    // // val message1: String = "text ecdsa with sha256"
    // // val message2: String = "text ecdsa with sha256 other message"
    // // ecdsaSign.update(message1.toByteArray(Charsets.UTF_8))
    // // val signature: ByteArray = ecdsaSign.sign()
    //
    // // val pub: String = Base64.getEncoder().encodeToString(publicKey.encoded)
    // // val sig: String = Base64.getEncoder().encodeToString(signature)
    //
    // // println("Encoded public key: $pub")
    // // println("Signature: $sig")
    //
    // // ecdsaSign.initVerify(publicKey);
    // // ecdsaSign.update(message1.toByteArray(Charsets.UTF_8));
    // // val result: Boolean = ecdsaSign.verify(signature)
    // // println("Signature is valid: $result")
    //
    // val input: String = "01000000018dd4f5fbd5e980fc02f35c6ce145935b11e284605bf599a13c6d415db55d07a1000000001976a91446af3fb481837fadbb421727f9959c2d32a3682988acffffffff0200719a81860000001976a914df1bd49a6c9e34dfa8631f2c54cf39986027501b88ac009f0a5362000000434104cd5e9726e6afeae357b1806be25a4c3d3811775835d235417ea746b7db9eeab33cf01674b944c64561ce3388fa1abd0fa88b06c44ce81e2234aa70fe578d455dac0000000001000000"
    // val finalHash: String = doubleHashSha256(input).toHex()
    // val bigEndianHash = doubleHashSha256(input)
    // val a = bigEndianHash.reversedArray().toHex()
    // println(a)
    //
    // val pubKeyExample1 = PubKeySB(
    //     x = BigInteger("2e930f39ba62c6534ee98ed20ca98959d34aa9e057cda01cfd422c6bab3667b7", 16),
    //     y = BigInteger("6426529382c23f42b9b08d7832d4fee1d6b437a8526e59667ce9c4e9dcebcabb", 16),
    // )
    // pubKeyExample1.printPubKey()
    //
    // val pubkey = pubKeyExample1.pubKey
    //
    // // one endian
    // // val message = "692678553d1b85ccf87d4d4443095f276cdf600f2bb7dd44f6effbd7458fd4c2".toByteArray()
    // // other endian
    // val message = "c2d48f45d7fbeff644ddb72b0f60df6c275f0943444d7df8cc851b3d55782669".toByteArray()
    //
    // val signature = "30450221009908144ca6539e09512b9295c8a27050d478fbb96f8addbc3d075544dc41328702201aa528be2b907d316d2da068dd9eb1e23243d97e444d59290d2fddf25269ee0e".toByteArray()
    // val ecdsaVerify = Signature.getInstance("NONEwithECDSA")
    // ecdsaVerify.initVerify(pubkey)
    // ecdsaVerify.update(message)
    // val result: Boolean = ecdsaVerify.verify(signature)
    // println("Signature is valid: $result")
    //
    // // example 2
    // val pubKeyExample2 = PubKeySB(
    //     x = BigInteger("1e3749c502cc12bc2bdcdef3309d5b5a287e59b0880c33b0241c2dd95f75542d", 16),
    //     y = BigInteger("84c6d7491dec5cc17c610d2ac95f6ada2231e8c164dd417054a5a1e1f0aa3b12", 16),
    // )
    //
    // val pubkey2 = pubKeyExample1.pubKey
    // val message2 = "".toByteArray()
    // val signature2 = "".toByteArray()
    //
    // val ecdsaVerify2 = Signature.getInstance("NONEwithECDSA")
    // ecdsaVerify2.initVerify(pubkey2)
    // ecdsaVerify2.update(message2)
    // val result2: Boolean = ecdsaVerify2.verify(signature2)
    // println("Signature is valid: $result2")

    val tx0: Tx = Tx(hexTx3)
    println("Full tx size is ${tx0.txSize} bytes")
    println("Txid: ${tx0.txid}")
    println("Version number: ${tx0.version}")
    println("Number of inputs: ${tx0.numInputs}")
    println("Inputs: ${tx0.inputs}")
    println("Number of outputs: ${tx0.numOutputs}")
    println("Outputs: ${tx0.outputs}")
    println(tx0.locktime)
}
