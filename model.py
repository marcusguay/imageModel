# This Python 3 environment comes with many helpful analytics libraries installed
# It is defined by the kaggle/python Docker image: https://github.com/kaggle/docker-python
# For example, here's several helpful packages to load

import numpy as np # linear algebra
import pandas as pd # data processing, CSV file I/O (e.g. pd.read_csv)
import tensorflow as tf # tensorflow
from tensorflow.keras import layers
from tensorflow.keras.models import load_model
from keras.callbacks import EarlyStopping
import matplotlib.pyplot as plt
import sys



img_height = 150
img_width = 150
batch_size = 1024
TIMES_TO_RUN = 1

data_dir = "C:\\Users\\Administrator\\app\Images\\training"

print(tf.config.list_physical_devices('GPU'))

INPUT_TENSOR = None
OUTPUT_TENSOR = None
NUM_OUTPUTS = 628

def show(image, label):
   plt.figure()
   plt.imshow(image, cmap='gray')
   plt.title("image")
   plt.axis('off')
   plt.show()


def processImage(path,label):
    image =  tf.io.read_file(path)
    image = tf.io.decode_image(image,expand_animations = False,channels = 1, dtype = tf.dtypes.uint8)
    image = tf.image.convert_image_dtype(image, dtype = tf.int8 , saturate=False, name=None)
   #show(image, "Lol")
    return image



LABEL = -2
OutputList = []
InputList = []
import os
for dirname, _,filenames in os.walk(data_dir):
     LABEL = LABEL + 1
     print(dirname)
     for filename in filenames:
      #print(filename)
      if INPUT_TENSOR == None:
       InputList.append(processImage(os.path.join(dirname,filename),dirname))
       OutputList.append(LABEL)
      
         
     


print(OutputList)
INPUT_TENSOR = tf.stack(InputList)
OUTPUT_TENSOR = tf.one_hot(OutputList,NUM_OUTPUTS)

#print(INPUT_TENSOR)
print(OUTPUT_TENSOR)

model = tf.keras.Sequential(
[
   
tf.keras.layers.Dense(64, activation = 'relu'),
tf.keras.layers.Flatten(),
tf.keras.layers.Dropout(0.2),
tf.keras.layers.Dense(32, activation = 'relu'),
tf.keras.layers.Dropout(0.2),
tf.keras.layers.Dense(NUM_OUTPUTS, activation = 'softmax')
]
)

model.compile(
optimizer = tf.keras.optimizers.Adam(learning_rate=1e-3),
loss = tf.keras.losses.CategoricalCrossentropy(),
metrics = 'accuracy'
)

early_stopping_monitor = EarlyStopping(
    monitor='val_loss',
    min_delta=0,
    patience=50,
    verbose=1,
    mode='auto',
    baseline=None,
    restore_best_weights=True
)








while (TIMES_TO_RUN > 0):
   model.fit(
   x  = INPUT_TENSOR,
   y = OUTPUT_TENSOR,
   epochs = 50,
   batch_size = 1024,
    shuffle = True,
   validation_split = 0.2,
   callbacks = [early_stopping_monitor]
   )
   TIMES_TO_RUN = TIMES_TO_RUN - 1



model.save("C:\\Users\\Administrator\\Desktop\\Model")
