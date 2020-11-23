#func3d.py
import numpy as np
import mpl_toolkits.mplot3d
from matplotlib import pyplot as plt
from matplotlib import cm

x, y = np.mgrid[0:1:20j,1:100:20j]
z = 1 - np.power(x, y)

fig = plt.figure(figsize=(16,12))
ax = fig.gca(projection='3d')
ax.plot_surface(x,y,z,cmap=cm.ocean)


x0, y0 = 0.8, 25
z0 = 1 - x0 ** y0
plt.plot(x0, y0, z0, marker='o', markersize='8', c="red")


plt.show()