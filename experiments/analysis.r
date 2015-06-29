#!/bin/Rscript

library(ggplot2)

########
# DICE #
########


rd <- read.csv('data/real-dice-freq.csv')
vd <- read.csv('data/virtual-dice-freq.csv')

reals <- rep('reais', length(rd$freq))
virtuals <- rep('virtuais', length(vd$freq))
dice <- data.frame(face = c(rd$face, vd$face), freq=c(rd$freq, vd$freq), type = c(reals,  virtuals))

dice

pdf('dice-freq.pdf', width=4, height=3.25)

rdAvg <- sum(rd$freq) / 6
rdAvg
vdAvg <- sum(vd$freq) / 6
vdAvg

ggplot(dice, aes(x=face, y=freq, fill=type))+#
geom_bar(stat='identity', position='dodge')+#
scale_x_continuous(breaks=c(1,2,3,4,5,6))+#
xlab('Face')+ylab('Frequência')+#
geom_hline(yintercept=rdAvg, linetype='dotted') +#
geom_hline(yintercept=vdAvg, linetype='dashed') +#
theme(legend.position='top'
        , legend.title=element_blank()
        , legend.background = element_rect(fill = "transparent",colour = NA)
        , plot.background = element_rect(fill = "transparent",colour = NA))

############
"Velocidade"
############

vt <- read.csv('data/virtual-time.csv')
rt <- read.csv('data/real-time.csv')

t.test(rt$r1, vt$r1, alternative='greater')
t.test(rt$r4, vt$r4, alternative='greater')

timing <- data.frame(
    player=unlist(list(rt$player, vt$player))
  , r1=c(rt$r1, vt$r1)
  , r4=c(rt$r4, vt$r4)
  , type=c(rep('reais', length(rt$r1)) , rep('virtuais', length(vt$r1))))

pdf('rl-vt-r1.pdf', width=4, height = 3.25, bg='transparent')
ggplot(timing, aes(x=type, y=r1, colour=type)) +#
geom_boxplot() +scale_x_discrete(breaks=NULL)+#
#geom_jitter(position = position_jitter(width=0.3))+#
theme(legend.position='top'
        , legend.title=element_blank()
        , legend.background = element_rect(fill = "transparent",colour = NA)
        , plot.background = element_rect(fill = "transparent",colour = NA))+#
xlab('')+ylab('Tempo[s]')

pdf('rl-vt-r4.pdf', width=4, height = 3.25)
ggplot(timing, aes(x=type, y=r4, colour=type)) +#
geom_boxplot() +scale_x_discrete(breaks=NULL)+#
#geom_jitter(position = position_jitter(width=0.3))+#
    theme(legend.position='top'
        , legend.title=element_blank()
        , legend.background = element_rect(fill = "transparent",colour = NA)
        , plot.background = element_rect(fill = "transparent",colour = NA))+#
xlab('')+ylab('Tempo[s]')

###############
# Qualitative #
###############


ans <- read.csv('data/answers-parse.csv')
auxilio <- data.frame(prat = ans$Em.relação.aos.critérios.abaixo..você.achou.melhor.ter.ou.não.auxílio.de.computador...Praticidade.
                    , conf = ans$Em.relação.aos.critérios.abaixo..você.achou.melhor.ter.ou.não.auxílio.de.computador...Confiabilidade..ex..trapacear..dados.viciados..
                    , div = ans$Em.relação.aos.critérios.abaixo..você.achou.melhor.ter.ou.não.auxílio.de.computador...Diversão.)

pdf('aux-praticidade.pdf', width=4, height=3.25)
ggplot(auxilio, aes(x=prat))+#
geom_bar() +#
theme(legend.position='top'
        , legend.title=element_blank()
        , legend.background = element_rect(fill = "transparent",colour = NA)
        , plot.background = element_rect(fill = "transparent",colour = NA))+#
    ylab('Frequência') + xlab('')

pdf('aux-confiabilidade.pdf', width=4, height=3.25)
ggplot(auxilio, aes(x=conf))+#
geom_bar() +#
theme(legend.position='top'
        , legend.title=element_blank()
        , legend.background = element_rect(fill = "transparent",colour = NA)
        , plot.background = element_rect(fill = "transparent",colour = NA))+#
    ylab('Frequência') + xlab('')

pdf('aux-diversão.pdf', width=4, height=3.25)
ggplot(auxilio, aes(x=div))+#
geom_bar() +#
theme(legend.position='top'
        , legend.title=element_blank()
        , legend.background = element_rect(fill = "transparent",colour = NA)
        , plot.background = element_rect(fill = "transparent",colour = NA))+#
ylab('Frequência') + xlab('')

####

pdf('dice-praticidade.pdf', width=4, height=3.25)
dados <- data.frame(prat = ans$Em.relação.aos.critérios.abaixo..que.tipo.de.dados.você.achou.melhor...Praticidade.
                  , conf = ans$Em.relação.aos.critérios.abaixo..que.tipo.de.dados.você.achou.melhor...Confiabilidade..ex..trapacear..dados.viciados..
                  , div = ans$Em.relação.aos.critérios.abaixo..que.tipo.de.dados.você.achou.melhor...Diversão.
                    )

pdf('dados-praticidade.pdf', width=4, height=3.25)
ggplot(dados, aes(x=prat))+#
geom_bar() +#
theme(legend.position='top'
        , legend.title=element_blank()
        , legend.background = element_rect(fill = "transparent",colour = NA)
        , plot.background = element_rect(fill = "transparent",colour = NA))+#
    ylab('Frequência') + xlab('')

pdf('dados-confiabilidade.pdf', width=4, height=3.25)
ggplot(dados, aes(x=conf))+#
geom_bar() +#
theme(legend.position='top'
        , legend.title=element_blank()
        , legend.background = element_rect(fill = "transparent",colour = NA)
        , plot.background = element_rect(fill = "transparent",colour = NA))+#
    ylab('Frequência') + xlab('')

pdf('dados-diversão.pdf', width=4, height=3.25)
ggplot(dados, aes(x=div))+#
geom_bar() +#
theme(legend.position='top'
        , legend.title=element_blank()
        , legend.background = element_rect(fill = "transparent",colour = NA)
        , plot.background = element_rect(fill = "transparent",colour = NA))+#
ylab('Frequência') + xlab('')
