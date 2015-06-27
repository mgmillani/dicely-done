#!/bin/python3

import sys

class Round():
	pass

argv = sys.argv

if len(argv) == 1:
	print("usage: %s <FILES...>"%(argv[0]))
else:
	gameDurations = []
	rounds = []
	players = {}
	for fl in argv[1:]:
		with open(fl) as f:
			lines = f.readlines()
			tg = 0
			tr = 0
			player = ''
			for l in lines:
				words = l.split(" ")
				t = float(words[0][:-2])
				#print(t)
				if words[2] == 'startgame':
					tg = t
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
	# print("Duration of each game:")				
	# for g in gameDurations:
	# 	print(g)
	print("player, r1, r2, r3, r4")
	for p in players:
		print(p, end=', ')
		for r in players[p][:-1]:
			print(r.t, end=', ')
		print(players[p][-1].t)
		#print(r.t, r.player, r.n)
