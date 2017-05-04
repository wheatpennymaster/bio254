from __future__ import print_function

import tensorflow as tf
from tensorflow.contrib import rnn
import numpy as np
import gc

# ==========
#  GET DATA
# ==========

# This is a thing
D = {'M': 1,'T': 2,'L': 3,'K': 4,'E': 5,'I': 6,'A': 7,'D': 8,'S': 9,'F': 10,'Y': 11,'P': 12,'G': 13,'W': 14,'N': 15,'Q': 16,'V': 17,'R': 18,'H': 19,'C': 20,'X': 21,'B':22,'Z':23,'U':24,'O':25,'J':26}

# Assigning each GO class to a continuous string of numbers; eventually, the label is converted into a one-hot 63-length vector
D2 = {'0016491': 0,'0003677': 1,'0005524': 2,'0046872': 3,'0016787': 4,'0016740': 5,'0000287': 6,'0051537': 7,'0051539': 8,'0008168': 9,'0016597': 10,'0005525': 11,'0050660': 12,'0003723': 13,'0010181': 14,'0015078': 15,'0046933': 16,'0004518': 17,'0003861': 18,'0003746': 19,'0016151': 20,'0019843': 21,'0003735': 22,'0009982': 23}

class GetData(object):
	# Read in sequence, label, and length files
	# Convert alphabet into numerical; one-hot to follow (later)
	def convert(self,aa,max_seq_len):
		arr = []
		for c in aa:
			arr.append([D[c]])
		arr += [[0] for i in range(max_seq_len - len(aa))]
		return arr

	def to_one_hot(self,a,r):
		if len(a) == 1:
			b = np.zeros(r,dtype=np.float32)
			b[a]=1
			return b
		else:
			bb = []
			for n in a:
				b = np.zeros(r,dtype=np.float32)
				b[n[0]]=1
				bb.append(b)
			return bb

	def __init__(self, max_seq_len=500, min_seq_len=2,path="/scratch/ttavolar/data/one_class/out/"):
		self.data = []
		self.labels = []
		self.seqlen = []
		#i=0
		with open(path+"sequences.txt", "r") as seq:
			for line in seq:
				line = line.replace('\n', '').replace('\r', '')
				l = self.convert(line,max_seq_len)
				l = self.to_one_hot(l,26) #tf.one_hot(l, 27, on_value=1.0, off_value=0.0, dtype="float")
				self.data.append(l)
				#i+=1
				#print (i)
		print ("Done reading sequences")
		with open(path+"lengths.txt", "r") as lenz:
			for line in lenz:
				line = line.replace('\n', '').replace('\r', '')
				self.seqlen.append(int(line))
		print ("Done reading lengths")
		with open(path+"go_classes.txt", "r") as lab:
			for line in lab:
				line = line.replace('\n', '').replace('\r', '')
				l = self.to_one_hot([D2[line]],24) #tf.one_hot([D2[line]],52,on_value=1.0, off_value=0.0, dtype="float")
				self.labels.append(l)
		print ("Done reading classes")
		self.batch_id = 0

	def next(self, batch_size):
		# Return a batch of data. When dataset end is reached, start over.
		if self.batch_id == len(self.data):
			self.batch_id = 0
		batch_data = (self.data[self.batch_id:min(self.batch_id + batch_size, len(self.data))])
		batch_labels = (self.labels[self.batch_id:min(self.batch_id + batch_size, len(self.data))])
		batch_seqlen = (self.seqlen[self.batch_id:min(self.batch_id + batch_size, len(self.data))])
		self.batch_id = min(self.batch_id + batch_size, len(self.data))
		return batch_data, batch_labels, batch_seqlen

# ==========
#   MODEL
# ==========

# Parameters
learning_rate = 0.0001
training_iters = 10000000
batch_size = 64
display_step = 10

# Network Parameters
seq_max_len = 500 # max protein length
n_hidden = 256 # hidden layer num of features
n_classes = 24 # number of functional classes

# Define test and training sets
trainset = GetData(max_seq_len=seq_max_len, min_seq_len=2,path="/scratch/ttavolar/data/one_class/out/train/")
gc.collect()
testset = GetData(max_seq_len=seq_max_len, min_seq_len=2,path="/scratch/ttavolar/data/one_class/out/test/")
print ("Done reading in data")

# tf Graph input
x = tf.placeholder("float", [None, seq_max_len, 26]) #Changing to 27 b/c it's one hot vector
y = tf.placeholder("float", [None, n_classes])
# A placeholder for indicating each sequence length
seqlen = tf.placeholder(tf.int32, [None])

# Define weights
weights = {
    'out': tf.Variable(tf.random_normal([2*n_hidden, n_classes]))
}
biases = {
    'out': tf.Variable(tf.random_normal([n_classes]))
}

def dynamicBiLSTM(x, seqlen, weights, biases):

	# Unstack to get a list of 'n_steps' tensors of shape (batch_size, n_input)
	x = tf.unstack(x, seq_max_len, 1)

	# Define LSTM forward and backward cells
	lstm_fw_cell = rnn.BasicLSTMCell(n_hidden, forget_bias=1.0)
	lstm_bw_cell = rnn.BasicLSTMCell(n_hidden, forget_bias=1.0)

	# Dropout wrapper
	lstm_fw_cell = rnn.DropoutWrapper(cell=lstm_fw_cell, output_keep_prob=0.5)
	lstm_bw_cell = rnn.DropoutWrapper(cell=lstm_bw_cell, output_keep_prob=0.5)

	# Get LSTM output; 'sequence_length' will hopefully make it dynamic
	outputs, _, _ = rnn.static_bidirectional_rnn(lstm_fw_cell, lstm_bw_cell, x, dtype=tf.float32, sequence_length=seqlen)

	# 'outputs' is a list of output at every timestep, we pack them in a Tensor
	# and change back dimension to [batch_size, n_step, n_input]
	outputs = tf.stack(outputs)
	outputs = tf.transpose(outputs, [1, 0, 2])

	# Hack to build the indexing and retrieve the right output.
	batch_size = tf.shape(outputs)[0]
	# Start indices for each sample
	index = tf.range(0, batch_size) * seq_max_len + (seqlen - 1)
	# Indexing
	outputs = tf.gather(tf.reshape(outputs, [-1, 2*n_hidden]), index)

	# Linear activation, using outputs computed above
	return tf.matmul(outputs, weights['out']) + biases['out']

pred = dynamicBiLSTM(x, seqlen, weights, biases)
print ("Done building network")

# Define loss and optimizer
cost = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(logits=pred, labels=y))
optimizer = tf.train.AdamOptimizer(learning_rate=learning_rate).minimize(cost)

# Evaluate model
correct_pred = tf.equal(tf.argmax(pred,1), tf.argmax(y,1))
accuracy = tf.reduce_mean(tf.cast(correct_pred, tf.float32))

# Initializing the variables
init = tf.global_variables_initializer()

print ("Launching")
# Launch the graph
with tf.Session() as sess:
	sess.run(init)
	step = 1
	# Keep training until reach max iterations
	while step * batch_size < training_iters:
		batch_x, batch_y, batch_seqlen = trainset.next(batch_size)
		#for i, x in enumerate(batch_x):
		#	batch_x[i] = x.eval()
		#for i, x in enumerate(batch_y):
		#	batch_y[i] = y.eval()
		#print (batch_x[0][0],"\n\n",batch_y,"\n\n",batch_seqlen[0])
		# Run optimization
		sess.run(optimizer, feed_dict={x: batch_x, y: batch_y, seqlen: batch_seqlen})
		if step % display_step == 0:
			# Calculate batch accuracy
			acc = sess.run(accuracy, feed_dict={x: batch_x, y: batch_y, seqlen: batch_seqlen})
			# Calculate batch loss
			loss = sess.run(cost, feed_dict={x: batch_x, y: batch_y, seqlen: batch_seqlen})
			print("Iter " + str(step*batch_size) + ", Minibatch Loss= " + \
				"{:.6f}".format(loss) + ", Training Accuracy= " + \
				"{:.5f}".format(acc))
		step += 1
	print("Optimization Finished!")

	# Calculate accuracy
	b_x, b_y, b_s = testset.next(batch_size)
	total=0.0
	i=0
	bid = 0
	while 1:
		total=total+sess.run(accuracy, feed_dict={x: b_x, y: b_y, seqlen: b_s})
		i=i+1
		b_x, b_y, b_s = testset.next(batch_size)
		if testset.batch_id<=bid:
			break
		else:
			bid=testset.batch_id
	print("Testing Accuracy:",total/i)
