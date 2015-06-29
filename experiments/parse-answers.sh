#!/bin/bash
cat data/answers.csv | sed 's/Com auxílio/Com/g' | sed 's/Sem auxílio/Sem/g' | sed 's/(muito melhor)/\+/g' | sed 's/Dados //g' > data/answers-parse.csv
