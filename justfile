default:
  just --list

test:
  ./gradlew test

publishlocal:
  ./gradlew publishToMavenLocal
