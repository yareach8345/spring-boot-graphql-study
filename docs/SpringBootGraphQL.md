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

### 2-1 単一ファイル

まず、graphqlsファイルを作成して、データのスキーマとエントリーポイントを定義しました。

Subscriptionのエントリーポイントは実務ではあまり使われなさそうですが、ここでは実習のために追加しました。

```graphql
# src/main/resources/graphql/schema.graphqls
# ファイルを分けたゆえ、このファイルの内容は変更されています。
# 変更前の内容を確認するには、`only-one-schema-file`ブランチの最終コミットを参照してください。

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
    getWriters(pageRequest: PageInput): [Writer!]!

    searchWriterByName(name: String!, pageRequest: PageInput): [Writer!]!

    getBook(id: ID!): Book
    getAllBooks(pageRequest: PageInput): [Book!]!
}

# データの取得以外の作業のためのMutation定義
type Mutation {
    addWriter(input: newWriterInfo!): Writer!
    deleteWriter(id: ID!): Boolean!

    addBook(input: newBookInfo!): Book!
    deleteBook(id: ID!): Boolean!
}

# 入力データ
input PageInput {
    page: Int!
    size: Int
}

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

### 2-2 複数のファイル

この例は簡単なのであって、graphqlsのファイルの内容が53行で終わりますが、実務ではもっと多くの種類のデータとエントリーポイントを扱うので、全てを一つのファイルに定義すると非常に長いファイルになる恐れがあります。
そのため、スキーマを作成する場合は複数のファイルに内容を分けて記録する必要があります。

Spring Boot GraphQLはビルドの際に`src/main/resources/graphql`の中の全てのファイルを自動的にスキャンします。
スキャンの対象はファイル名ではなく、ファイルの位置によって決まるため、`src/main/resources/graphql`の中に複数の.graphqlsファイルを作成することができます。

では、ファイルを分けてみましょう。

まず、schema.graphqlsを作成します。

ここでは共通的に使用される基本タイプを定義します。
また、このファイルでは空のQuery・Mutation・Subscriptionも定義します。
それらがここで定義される理由はGraphQLがタイプの重複を許可していないためです。
そのため、ファイルごとにエントリーポイントを定義するためには、一度基本タイプを定義した後、他のファイルで`extend`を使ってタイプを拡張する形で使用する必要が有るからです。

```graphql
# src/main/resources/graphql/schema.graphqls

input PageInput {
    page: Int!
    size: Int
}

type Query {}

type Mutation {}

type Subscription {}
```

他のファイルではそのファイルに関するタイプを定義し、
schema.graphqlsで定義した基本タイプを拡張してエントリーポイントを追加します。

```graphql
# src/main/resources/graphql/book.graphqls

# データスキーマ定義
type Book {
    id: ID!
    title: String!
    description: String
    writer: Writer!
}

# schema.graphqlsの　Queryを拡張
# 本を検索するためのエンドポイントを追加
extend type Query {
    getBook(id: ID!): Book
    getAllBooks(pageRequest: PageInput): [Book!]!
}

# schema.graphqlsの　Mutationを拡張
# 本に対する作業のめのエンドポイントを追加
extend type Mutation {
    addBook(input: newBookInfo!): Book!
    deleteBook(id: ID!): Boolean!
}

input newBookInfo {
    title: String!
    description: String
    writerId: ID!
}

# schema.graphqlsの Subscriptionを拡張
extend type Subscription {
    bookRegistered: Book

    bookRegisteredByWriter(writerId: ID!): Book
}
```

```graphql
# src/main/resources/graphql/writer.graphqls

type Writer {
    id: ID!
    name: String!
    description: String
    books: [Book!]!
}

# schema.graphqlsの　Queryを拡張
# 著者を検索するためのエンドポイントを追加
extend type Query {
    getWriter(id: ID!): Writer!
    getWriters(pageRequest: PageInput): [Writer!]!

    searchWriterByName(name: String!, pageRequest: PageInput): [Writer!]!
}

# schema.graphqlsの　Mutationを拡張
# 著者に対する作業のめのエンドポイントを追加
extend type Mutation {
    addWriter(input: newWriterInfo!): Writer!
    deleteWriter(id: ID!): Boolean!
}

# Mutationで使用する入力データ
input newWriterInfo {
    name: String!
    description: String
}
```