今回はGraphQLの実習のためSpring Boot + GraphQLで簡単なAPIを構築してみます。

本記事ではSpring Bootを使ってGraphQL APIの構築だけについて記述します。
GraphQL自体の説明やR2DBC・Kotlin・CoroutineなどのGraphQL以外の記述は別に扱っていないため、それについては別の資料をご参考ください。

# 構築環境

今回のAPI構築環境は下記のようです。

1. Spring Boot (4.x)
2. Spring Boot Graphql (4.x)
3. Kotlin (2.2.x)
4. Coroutine (1.10.x)
5. R2DBC
6. MySQL