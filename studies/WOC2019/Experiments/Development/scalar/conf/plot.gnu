#set terminal dumb
set terminal pdfcairo dashed
set output 'report.pdf'



###########
# USL FIT #
###########

f(x) = a*a * x**2 + (a*a+b*b) * x # 0% logout

set fit quiet
fit f(x) 'gnuplot-capacity.dat' using ($1-1):(($1/$2)-1) via a,b

sigma = b*b
kappa = a*a

set print "-"
print sprintf("sigma=%e", sigma)
print sprintf("kappa=%e", kappa)

# Universal scaleup model
C(p) = p / (1 + sigma * (p-1) + kappa * p * (p-1))

# Linear scaling
L(p) = p

stats 'gnuplot-capacity.dat' using 1

set key left top
set ylabel "Effective users handled"
set xlabel "Load (concurrent users)"
set xrange [0:STATS_max*1.5]
set y2label "Percentage of requests"
set y2range[0:100]

plot L(x) title "Linear scaling" lc rgb "#666666" lw 1 dashtype "--",\
     'gnuplot-capacity.dat' using 1:2 title "Measurements" with points lc rgb "#444444" pt 7 ps 0.7,\
     C(x) title "USL fit" lt 1 lc rgb "#222222" lw 2,\
     'gnuplot-capacity.dat' using 1:4 title "Slow requests (%)" with linespoints lc rgb "#666666" pt 9 ps 0.7 axes x1y2


############################
# Residence times heat map #
############################

stats 'gnuplot-heatmap.dat' using 10

set ylabel "Residence time (ms)"
set xlabel "User load"
set nokey
set view map
set palette negative grey
set cbrange [0:100]
unset xrange
set yrange [0:STATS_mean]
unset y2label

splot 'gnuplot-heatmap.dat' using 2:10:12 title "Residence time distribution" with image


###########################
# Think time distribution #
###########################

stats 'gnuplot-heatmap.dat' using 16
set ylabel "Think time (ms)"
set yrange [0:STATS_mean]
splot 'gnuplot-heatmap.dat' using 2:16:18 title "Think time distribution" with image


#####################################
# Start versus end time of requests #
#####################################

set key
set xlabel "Time (s)"
set ylabel "Request count"
unset yrange

plot 'gnuplot-heatmap.dat' using ($4/1000):5 title "Started" with points lc rgb "#444444" pt 7 ps 0.7,\
     'gnuplot-heatmap.dat' using ($4/1000):8 title "Stopped" with linespoints lc rgb "#666666" pt 9 ps 0.7 axes x1y2

