# Sugestão de protocolo para o Dicely Done

O protocolo deve ser implementado sobre TCP.

#### Notação

As mensagens entre `[]` são para a versão totalmente virtual.

As mensagens precedidas de `*` podem ser enviadas zero ou mais vezes.

As mensagens precedidasde `+` podem ser enviadas uma ou mais vezes.

O uso de `|` separando mensagens na mesma linha indica que qualquer das duas pode ser enviada, portanto as duas mensagens devem ser aceitas nesse momento.

## Pré-jogo:

    Client                      Server
    ----------------------------------------
    join <player name>
                                joined

A mensagem `join <name>` registra o nome do jogador no servidor.

## Jogo típico:

    Client                      Server
    --------------------------------------------------------------
                                startgame <startbet>

                               *dice <player> <d1> <d2> <d3> <d4> <d5>
                                startturn 1
    [roll]
                               +dice <d1> <d2> <d3> <d4> <d5>

                               *( betplaced <player> <singlebet> <totalbet>
                                | folded <player>
                                | endgame <winner> <amount won> )
                                startturn 2 <minbet>
    bet <val>
                               +( betplaced <player> <singlebet> <totalbet>
                                | folded <player>
                                | endgame <winner> <amount won> )

                                startturn 3 <bet>
    bet <val>
                               +( betplaced <player> <singlebet> <totalbet>
                                | folded <player>
                                | endgame <winner> <amount won> )

                               *dice <player> <d1> <d2> <d3> <d4> <d5>
                                startturn 4
    [reroll <d1> ... <dn>]
                               +dice <d1> <d2> <d3> <d4> <d5>

                                endgame <winner> <amount won>
    (restart | quit)
                                startgame <startbet>
                                ...

A mensagem `startgame <startbet>` indica que um jogo foi iniciado e cada jogador começará apostando <startbet>.

A mensagem `startturn <round> ...` indica que o turno de um jogador foi iniciado, na rodada passada. Além disso, nas rodadas de aposta (2 e 3), o valor mínimo da aposta é passado.

A mensagem `dice <player> <d1> ... <d5>` informa os jogadores sobre os dados obtidos pelo jogador passado. Cada valor `<di>` vai de 1 a 6.

A mensagem `roll` informa o servidor de que os dados devem ser sorteados. _Apenas na versão totalmente virtual_.

A mensagem `reroll <d1> ... <dn>` informa o servidor de que `5-n` dados devem ser sorteados, completando a mão com os `0 < n <= 5` dados passados. _Apenas na versão totalmente virtual_.

A mensagem `bet <val>` contém o valor apostado pelo jogador. O valor enviado contém o valor apostado anteriormente, isto é, deve ser usado como aposta total do jogador e não somado à aposta anterior.

A mensagem `betplaced <player> <singlebet> <totalbet>` informa os jogadores de que o jogador `<player>` apostou, desde o início do jogo `<singlebet>`, e que a soma das apostas dos jogadores é `<totalbet>`.

A mensagem `folded <player>` indica que o jogador passado desistiu do jogo atual.

A mensagem `victory <c1> <c5> <c10>` contém o número de fichas de cada tipo
que o jogador venceu ao fim do jogo.

A mensagem `endgame <winner> <amount won>` informa aos clientes qual jogador venceu e quanto ganhou. A única mensagem válida após essa é `startgame`. Normalmente ocorre após a rodada 4, porém pode ocorrer antes caso todos os jogadores (exceto um) desistam.

A mensagem `restart` informa ao servidor que o jogador está pronto para começar uma nova partida.

A mensagem `quit` informa ao servidor que o jogador sairá do jogo. Pode ser enviada a qualquer momento.

## Desistência:

    Client                      Server
    --------------------------------------------------------------
                                startturn <round> <minbet>
    fold

                               *( betplaced <player> <singlebet> <totalbet>
                                | folded <player>)

                               *dice <player> <d1> <d2> <d3> <d4> <d5>

                                endgame <winner> <amount won>

                                startgame
                                ...

A mensagem `fold` indica que o jogador desistiu do jogo atual.

## Desconectando o Cliente

O cliente pode ser desconectado acidental ou intencionalmente. Desconexão acidental é detectada por timeout durante a espera de uma mensagem `ack`. A desconexão intencional é informada pela mensagem `disconnect`, descrita abaixo.

Assim que a desconexão for detectada, ela deve ser informada aos clientes restantes através da mensagem `disconnected <player>`. A aposta que o jogador fez no jogo atual continua valendo, como se o jogador tivesse desistido fora de seu turno. Caso apenas um jogador continue conectado, ele vence automaticamente o jogo.

A mensagem `disconnect <player>` indica ao servidor que o jogador passado sairá do jogo definitivamente. Ela pode ser enviada pelo cliente a qualquer momento (inclusive na fase pré-jogo).

A mensagem `disconnected <player>` indica ao cliente que o jogador passado saiu do jogo definitivamente. Ela pode ser enviada pelo servidor a qualquer momento (exceto na fase pré-jogo).
