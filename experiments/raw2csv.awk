function time(t) {
	match(t, /([0-9]+\.[0-9]+)s/, ary)
	return "0:0:" ary[1]
}

BEGIN {
	turn=0
}

$0 ~ /startgame/ && turn==0 {
	startgame = time($1)
	turn = 1
}

$0 ~ /startturn 2/ && turn==1 {
	endTurn1 = time($1)
	turn=2
}

$0 ~ /startturn 3/ && turn==2 {
	endTurn2 = time($1)
	turn=3
}

$0 ~ /startturn 4/ && turn==3 {
	endTurn3 = time($1)
	turn=4
}

$0 ~ /endgame/ && 2 <= turn && turn <= 4 {
	endTurn4 = time($1)
	if (turn <= 3) endTurn3 = endTurn4
	if (turn <= 2) endTurn2 = endTurn4
	endGame = endTurn4
	turn=0
	print group ";" startgame ";" endTurn1 ";" endTurn2 ";" endTurn3 ";" endTurn4 ";" endGame ";"
}

