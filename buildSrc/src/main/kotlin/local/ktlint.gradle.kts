package local

val ktlint by configurations.creating

repositories {
    jcenter()
}
dependencies {
    ktlint("com.github.shyiko:ktlint:0.27.0")
}

val verifyKtlint by tasks.creating(JavaExec::class) {
    description = "Check *.gradle.kts code style."
    classpath = ktlint
    main = "com.github.shyiko.ktlint.Main"
    args("**/*.gradle.kts", "**/*.kt")
}
tasks["check"].dependsOn(verifyKtlint)

task("ktlint", JavaExec::class) {
    description = "Fix *.gradle.kts code style violations."
    classpath = verifyKtlint.classpath
    main = verifyKtlint.main
    args("-F")
    args(verifyKtlint.args)
}
