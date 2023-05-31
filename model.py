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

#print(tf.config.list_physical_devices('GPU'))

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
    # print(os.path.join(data_dir,path))\
    # print("processing iamge")
    # print(path.numpy())
     image =  tf.io.read_file(path)
     image = tf.io.decode_image(image,expand_animations = False,channels = 1, dtype = tf.dtypes.uint8)
     image = tf.image.convert_image_dtype(image, dtype = tf.int8 , saturate=False, name=None)
     #show(image, "Lol")
     label = tf.one_hot(label,NUM_OUTPUTS)
     return image, label
  


DATASET = None


LABEL = 0
NUM_IMG = 0
OutputList = []
InputList = []
import os
for dirname in os.listdir(data_dir):
     path = os.path.join(data_dir,dirname)
    
     #print("directory")
    # print(dirname)
     for filename in os.listdir(path):
         # print("file")
         print(os.path.join(dirname,filename))
        # processImage(os.path.join(dirname,filename),LABEL)
         InputList.append(os.path.join(data_dir,os.path.join(dirname,filename)))
         OutputList.append(LABEL)
         NUM_IMG = NUM_IMG + 1
     
     LABEL = LABEL + 1
     if DATASET == None:
      TEMP_INPUT_TENSOR = tf.convert_to_tensor(InputList)
      TEMP_OUTPUT_TENSOR =  tf.convert_to_tensor(OutputList)
      DATASET = tf.data.Dataset.from_tensor_slices((TEMP_INPUT_TENSOR,TEMP_OUTPUT_TENSOR))
      InputList = []
      OutputList = []
     else:    
      TEMP_INPUT_TENSOR = tf.convert_to_tensor(InputList)
      TEMP_OUTPUT_TENSOR =  tf.convert_to_tensor(OutputList)
      TEMPDATASET = tf.data.Dataset.from_tensor_slices((TEMP_INPUT_TENSOR,TEMP_OUTPUT_TENSOR))
      DATASET = DATASET.concatenate(TEMPDATASET)
      InputList = []
      OutputList = []

     
     


DATASET = DATASET.map(processImage)
DATASET = DATASET.shuffle(NUM_OUTPUTS)
Split =  NUM_IMG // (10/2)
TEST_SET = DATASET.take(Split)
BATCH_DATASET = DATASET.skip(Split)
BATCH_DATASET = BATCH_DATASET.batch(64)

for element in DATASET.as_numpy_iterator():
   print(element)


print(OUTPUT_TENSOR)

model = tf.keras.Sequential(
[
tf.keras.Input(shape=(150,150,1)),  
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
    monitor='accuracy',
    min_delta=0,
    patience=50,
    verbose=1,
    mode='auto',
    baseline=None,
    restore_best_weights=True
)


while (TIMES_TO_RUN > 0):
    model.fit(
    BATCH_DATASET,  
    epochs = 1,
    shuffle = True,
   callbacks = [early_stopping_monitor]
   )
    TIMES_TO_RUN = TIMES_TO_RUN - 1



# model.save("C:\\Users\\Administrator\\Desktop\\Model")
