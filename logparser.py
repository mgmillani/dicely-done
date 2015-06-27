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
	f = 1
	for r in virtual:
		print(f, r, end=', ', sep=',')
		print('virtual')
		f+=1
	
def csvRound(players):
	print("player, r1, r2, r3, r4")
	for p in players:
		print(p, end=', ')
		for r in players[p][:-1]:
			print(r.t, end=', ')
		print(players[p][-1].t)
		#print(r.t, r.player, r.n)


argv = sys.argv

if len(argv) == 1:
	print("usage: %s <FILES...>"%(argv[0]))
else:
	gameDurations = []
	rounds = []
	players = {}
	realDice = [0,0,0,0,0,0,0]
	virtualDice = [0,0,0,0,0,0,0]
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
				t = float(words[0][:-2])
				#print(t)
				if words[2] == 'startgame':
					tg = t
					virtual = False
					player = words[1]
				elif words[2] == 'endgame':
					gameDurations.append(t - tg)
					r = Round()
					r.t = t - tr
					r.player = player
					r.n = n
					rounds.append(r)
					#print(l, player, n)
					players[player].append(r)
				elif words[2] == 'startturn':
					if player != words[1]:
						r = Round()
						r.t = t - tr
						r.player = player
						r.n = n
						rounds.append(r)
						if not player in players:
							players[player] = []
						#print(l, player, n)
						players[player].append(r)
						player = words[1]
						
					tr = t
					n = int(words[3])
				elif words[2] == 'dice':
					for d in words[4:]:
						if virtual:
							virtualDice[int(d)] += 1
						else:
							realDice[int(d)] += 1
				elif words[2] == 'roll':
					virtual = True
					
	#csvRound(players)
	csvDice(realDice, virtualDice)

