# Readme
I build this parser to force myself to dig into the internals of bitcoin transactions, the fields they contain and the way to constructs the messages that get constructed and signed given the sighash flags provided.

This transaction parser currently only validates P2PKH transactions. I leave in many known bugs and unhandled corner cases that would make the readability and implementation less suitable for learning.

## Resources
1. [How does the ECDSA verification algorithm work during transaction?](https://bitcoin.stackexchange.com/questions/32305/how-does-the-ecdsa-verification-algorithm-work-during-transaction)  
2. [How do you get the OP_HASH160 value from a bitcoin address?](https://bitcoin.stackexchange.com/questions/5021/how-do-you-get-the-op-hash160-value-from-a-bitcoin-address)  
3. [Bitcoin Bytes — Legacy Transactions ](https://thunderbiscuit.com/posts/transactions-legacy/)  
