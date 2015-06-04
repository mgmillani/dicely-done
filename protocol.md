# Sugestão de protocolo para o Dicely Done

As mensagens entre "[]" são para a versão totalmente virtual.


## Pré-jogo:

    Client                      Server
   ----------------------------------------
    join <player name>
                                ack

## Jogo típico:

    Client                      Server
   --------------------------------------------------------------
                                startgame
                                           
                                startturn 1
    ack
    [roll]
                                dice <d1> <d2> <d3> <d4> <d5>
                                
                                startturn 2 <minbet>
    ack
    bet <c1> <c5> <c10>
                                ack
                                
                                startturn 3 <bet>
    ack
    bet <c1> <c5> <c10>
                                ack
    
                                startturn 4
    ack
    [reroll <d1> ... <dn>]
                                dice <d1> <d2> <d3> <d4> <d5>
    
                                victory <c1> <c5> <c10>
                                defeat
                                
                                startgame
                                ...

A mensagem `dice <d1> ... <d5>` contém a mão do jogador cujo cliente a recebe.
Cada valor `<di>` vai de 1 a 6.

A mensagem `reroll <d1> ... <dn>` contém os valores de dados que serão mantidos
pelo jogador, com `0 < n <= 5`. Também é um pedido para que o servidor sorteie
`5-n` dados e envie a mão completa do jogador.

A mensagem `startturn 2 <minbet>` contém o valor mínimo de aposta que o jogador
deve colocar para continuar no jogo. A mensagem `startturn 3 <bet>` é análoga.

A mensagem `bet <c1> <c5> <c10>` contém o número de fichas de cada tipo
apostadas pelo jogador.

A mensagem `victory <c1> <c5> <c10>` contém o número de fichas de cada tipo
que o jogador venceu ao fim do jogo.


## Desistência:

    Client                      Server
   --------------------------------------------------------------
                                startturn 2 <minbet>
    fold
                                ack
                                
                                startgame
                                ...



