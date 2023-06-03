# This Python 3 environment comes with many helpful analytics libraries installed
# It is defined by the kaggle/python Docker image: https://github.com/kaggle/docker-python
# For example, here's several helpful packages to load

import numpy as np # linear algebra
import pandas as pd # data processing, CSV file I/O (e.g. pd.read_csv)
import tensorflow as tf # tensorflow
import tensorflow.keras
from tensorflow.keras import layers
from tensorflow.keras.models import load_model
from keras.callbacks import EarlyStopping
import matplotlib.pyplot as plt
import sys

print(tf.config.list_physical_devices('GPU'))

img_height = 150
img_width = 150
batch_size = 32
TIMES_TO_RUN = 10
NUM_IMG = 0

data_dir = "C:\\Users\\Administrator\\app\\Images\\Train"

INPUT_TENSOR = None
OUTPUT_TENSOR = None
NUM_OUTPUTS = 10



model = tf.keras.Sequential()

model.add(layers.Conv2D(64, (3,3), activation='relu', input_shape= (img_width,img_height,1)))
model.add(layers.Conv2D(64, (3,3), activation='relu'))
model.add(layers.MaxPooling2D((3,3)))
model.add(layers.Dropout(0.2))
model.add(layers.Conv2D(128, (3,3), activation='relu'))
model.add(layers.Conv2D(128, (3,3), activation='relu'))
model.add(layers.MaxPooling2D((3,3)))
model.add(layers.Dropout(0.2))
model.add(layers.Flatten())
model.add(layers.Dense(NUM_OUTPUTS, activation='softmax'))


sgd = tf.keras.optimizers.SGD(
    learning_rate=1e-5,
    momentum=0.9,
    decay=1e-6,

)
model.compile(
optimizer = sgd,
loss = tf.keras.losses.CategoricalCrossentropy(),
metrics = 'accuracy'
)



def show(image, label):
   plt.figure()
   plt.imshow(image, cmap='gray')
   plt.title("image")
   plt.axis('off')
   plt.show()


def processImage(path,label):
   
#     # print("processing iamge")
      #print(path)
      image =  tf.io.read_file(path)
      image = tf.io.decode_png(image,channels = 1, dtype = tf.dtypes.uint8)
      image = tf.image.convert_image_dtype(image, dtype = tf.int8 , saturate=False, name=None)
      image = tf.reshape(image,[img_width, img_height, 1])
      label = tf.one_hot(label,NUM_OUTPUTS,dtype = tf.float16)
      print(image)

      return image,label
  


DATASET = None


LABEL = 0
NUM_IMG = 0
OutputList = []
InputList = []
import os

for dirname in os.listdir(data_dir):
     path = os.path.join(data_dir,dirname)
   
     #print("directory")
     print(dirname + str(LABEL))
     for filename in os.listdir(path):
         # print("file")
        # print(os.path.join(dirname,filename))
        # processImage(os.path.join(dirname,filename),LABEL)
         InputList.append(os.path.join(data_dir,os.path.join(dirname,filename)))
         OutputList.append(LABEL)
         NUM_IMG = NUM_IMG + 1
         

     if DATASET == None:
      TEMP_INPUT_TENSOR = tf.convert_to_tensor(InputList)
      TEMP_OUTPUT_TENSOR =  tf.convert_to_tensor(OutputList)
      DATASET = tf.data.Dataset.from_tensor_slices((TEMP_INPUT_TENSOR,TEMP_OUTPUT_TENSOR))
      InputList = []
      OutputList = []
     else:    
      TEMP_INPUT_TENSOR = InputList
      TEMP_OUTPUT_TENSOR =  OutputList
      TEMPDATASET = tf.data.Dataset.from_tensor_slices((TEMP_INPUT_TENSOR,TEMP_OUTPUT_TENSOR))
      DATASET = DATASET.concatenate(TEMPDATASET)
      InputList = []
      OutputList = []
     LABEL = LABEL + 1
     
     

 
     
DATASET = DATASET.shuffle(NUM_IMG,reshuffle_each_iteration=True)

DATASET= DATASET.map(processImage)

NUM_OUTPUTS = 648

Split =  (int) (NUM_IMG // (10/2))
print(Split)
TEST_SET = DATASET.take(Split)
BATCH_DATASET = DATASET.skip(Split)
BATCH_DATASET = BATCH_DATASET.batch(batch_size)
TEST_SET = TEST_SET.batch(batch_size)




# msg = "Split "
# print(msg + str(Split))
# print("IMAGES :")
# print(NUM_IMG)
# print(NUM_IMG//batch_size)


# print(DATASET)
# print(BATCH_DATASET)
#  print(TEST_SET)

print(NUM_IMG)


#print("batch")
# 
# for element in TEST_SET:
#    print(element)





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
       validation_data= TEST_SET,
    epochs = 10,
    shuffle = True,
   callbacks = [early_stopping_monitor]
   )
    DATASET = DATASET.shuffle(NUM_OUTPUTS)
    TIMES_TO_RUN = TIMES_TO_RUN - 1
    TEST_SET = DATASET.take(Split)
    BATCH_DATASET = DATASET.skip(Split)
    BATCH_DATASET = BATCH_DATASET.batch(64)
#     print(DATASET)
#     print(BATCH_DATASET)
#     print(TEST_SET)
    model.save("C:\\Users\\Administrator\\Desktop\\Model")



