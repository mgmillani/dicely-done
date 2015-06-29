#!/bin/python3

import sys

class Round():
	pass

def csvDice(real, virtual):
	print("face,frequency,type")
	f = 1
	for r in real[1:]:
		print(f, r, end=', ', sep=',')
		print('real')
		f+=1

def csvRound(games):
	print("player, r1, r2, r3, r4")
	for g in games:
		players = g
		for p in players:
			print(p, end=', ')
			pad = len(players[p]) - 4
			for r in players[p][:-1]:
				print(r.t, end=', ')
			print(players[p][-1].t, end='')
			print(','*pad)


argv = sys.argv

if len(argv) == 1:
	print("usage: %s <FILES...>"%(argv[0]))
else:
	rounds = []
	players = {}
	realDice = [0,0,0,0,0,0,0]
	virtualDice = [0,0,0,0,0,0,0]
	dice = []
	games = []
	for fl in argv[1:]:
		with open(fl) as f:
			#print(fl)
			lines = f.readlines()
			tg = 0
			tr = 0
			player = ''
			n = 1
			for l in lines:
				words = l.split(" ")
				#print(l)
				t = float(words[0][:-2])
				#print(t)
				if words[2] == 'startgame':
					tg = t
					virtual = False
					player = words[1]
					players = {}
				elif words[2] == 'endgame':
					#gameDurations.append(t - tg)
					r = Round()
					r.t = t - tr
					r.player = player
					r.n = n
					rounds.append(r)
					players[player].append(r)
					games.append(players)
					#print(len(players[player]))
					players = {}
					player = ''
				elif words[2] == 'startturn':
					if player != words[1]:
						r = Round()
						r.t = t - tr
						r.player = player
						r.n = n
						rounds.append(r)
						if not player in players:
							players[player] = []
						players[player].append(r)
						#print(player, len(players[player]))
						player = words[1]
						for d in dice:
							realDice[d] += 1
						dice = []
						tr = t
						n = int(words[3])
				elif words[2] == 'dice' and n==1:
					dice = []
					for d in words[4:]:
						dice.append(int(d))

	csvRound(games)
	#csvDice(realDice, virtualDice)
