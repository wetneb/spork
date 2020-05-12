#! /bin/bash

if [ ! -f ~/jars/sootdiff.jar ]; then
  mkdir -p ~/jars
  echo "Fetching jars"
  curl -L https://github.com/slarse/sootdiff/releases/download/spork-experiment/sootdiff-1.0-jar-with-dependencies.jar -o ~/jars/sootdiff.jar
  curl -L https://github.com/slarse/duplicate-checkcast-remover/releases/download/v1.0.0/duplicate-checkcast-remover-1.0.0-jar-with-dependencies.jar -o ~/jars/duplicate-checkcast-remover.jar
  curl -L https://github.com/slarse/pkgextractor/releases/download/v1.0.0/pkgextractor-1.0.0-jar-with-dependencies.jar -o ~/jars/pkgextractor.jar
  ls ~/jars
fi

echo "Compiling spork"
mvn clean compile package -DskipTests
spork_jar_path="$PWD/$(ls target/spork*.jar)"

echo "Creating spork executable"
echo "#! /bin/bash" > spork
echo "$JAVA_HOME/bin/java -jar $spork_jar_path --exit-on-error" '$@' >> spork
chmod 700 spork

mkdir -p ~/.local/bin
cp "$TRAVIS_BUILD_DIR"/.travis/{pkgextractor,sootdiff,duplicate-checkcast-remover,run_benchmark.sh,run_build_benchmark.sh} spork ~/.local/bin

git checkout benchmark

cp "$TRAVIS_BUILD_DIR"/merge_driver_config/gitconfig ~/.gitconfig
cp "$TRAVIS_BUILD_DIR"/merge_driver_config/filemergelocator ~/.local/bin

ls -l ~/.local/bin

# required for running the git merges
git config --global user.email example@example.com
git config --global user.name example

cd "$TRAVIS_BUILD_DIR"/scripts || exit
python3 -m pip install -r requirements.txt
