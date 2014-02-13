
#MAKE SURE YOU CHOOSE AN EDGE FILE
edf = read.table(file.choose( ),sep=",",stringsAsFactors=FALSE)
#MAKE SURE YOU CHOOSE THE CORRESPONDING YEAR FILE
vdf = read.table(file.choose( ),sep=",",stringsAsFactors=FALSE)

g = graph.data.frame(edf,directed=F,vertices=vdf)

col = vector(length = length(vdf$V4))
for(i in 1:length(vdf$V4)){
	if(vdf$V4[i] == "WHITE"){
		col[i] = "blue"
	}else{
		col[i] = "red"
	}
}
V(g)$color <- col
plot(g)