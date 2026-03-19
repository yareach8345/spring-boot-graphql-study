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

# 構築

では、構築を始めてみましょう。

## 1. 依存関係

下のコードはbuild.gradleファイルの一部です。
Spring BootとGraphQLのための依存関係とそれ以外の必要な依存関係を追加します。

```groovy
dependencies {

    // spring boot & graph ql
    implementation 'org.springframework.boot:spring-boot-starter-security'
    testImplementation 'org.springframework.boot:spring-boot-starter-security-test'
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    testImplementation 'org.springframework.boot:spring-boot-starter-webflux-test'
    implementation 'org.springframework.boot:spring-boot-starter-graphql' // <- GraphQLを使うためのライブラリー
    testImplementation 'org.springframework.boot:spring-boot-starter-graphql-test' // <- GraphQLのテストのためのライブラリー
    developmentOnly 'org.springframework.boot:spring-boot-devtools'

    // database
    implementation 'org.springframework.boot:spring-boot-starter-r2dbc'
    testImplementation 'org.springframework.boot:spring-boot-starter-r2dbc-test'
    // mysql
    implementation 'io.asyncer:r2dbc-mysql'
    // h2 database for test
    testRuntimeOnly 'com.h2database:h2'
    testImplementation 'io.r2dbc:r2dbc-h2'

    // test
    testImplementation 'org.jetbrains.kotlin:kotlin-test-junit5'
    testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'

    // その他
    implementation 'io.projectreactor.kotlin:reactor-kotlin-extensions'
    implementation 'org.jetbrains.kotlin:kotlin-reflect'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-reactor'
    implementation 'tools.jackson.module:jackson-module-kotlin'
}
```

## 2. graphqlsファイル作成

まず、graphqlsファイルを作成して、データのスキーマとエントリーポイントを定義しました。

Subscriptionのエントリーポイントは実務ではあまり使われなさそうですが、ここでは実習のために追加しました。

```graphql
# src/main/resources/graphql/schema.graphqls

# データスキーマ定義
type Writer {
    id: ID!
    name: String!
    description: String
    books: [Book!]!
}

type Book {
    id: ID!
    title: String!
    description: String
    writer: Writer!
}

# データ取得のためのQuery定義
type Query {
    getWriter(id: ID!): Writer!
    getWriters(page: Int, size: Int): [Writer!]!

    searchWriterByName(name: String!, page: Int, size: Int): [Writer!]!

    getBook(id: ID!): Book
    getAllBooks: [Book!]!
    getBooksWithPaging(first: Int!, offset: Int): [Book!]!
}

# データの取得以外の作業のためのMutation定義
type Mutation {
    addWriter(input: newWriterInfo!): Writer!
    deleteWriter(id: ID!): Boolean!

    addBook(input: newBookInfo!): Book!
    deleteBook(id: ID!): Boolean!
}

# Mutationで使用する入力データ
input newWriterInfo {
    name: String!
    description: String
}

input newBookInfo {
    title: String!
    description: String
    writerId: ID!
}

# Subscription定義
type Subscription {
    bookRegistered: Book

    bookRegisteredByWriter(writerId: ID!): Book
}
```