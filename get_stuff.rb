#!/bin/ruby

# This gets bacterial protein sequences from uniprot_sprot.dat, their GO annotation(s), and their length; all of this is written to separate files
require 'pp'

path = "/scratch/ttavolar/data/uniprot_sprot.dat"
text = File.read(path)
max_len = 500
min_sam = 1000

arr = text.split("//\n")

# Count occurance of GO terms for bacterial protein sequences; grab the sequence, length, and annotation
record = []
data = []
arr.each do |r,index|
	if r.include?("OC   Bacteria")
		l = r.scan(/SQ   SEQUENCE   ([0-9]+) AA/)
		l = l[0][0]
		#print "Length: "
		#puts l
		s = r.scan(/SQ   SEQUENCE.*\n([^*]*)/)
		s = s[0][0]
		s.gsub!(/\s+/, "")
		#print "Sequence: "
		#puts s
		m = r.scan(/GO:([0-9]+); F/)
		m.each do |ii|
			# Enforce length here
			if l.to_i < max_len
				i = ii[0]
				data << [s,l,i]
				#print "GO: "
				#puts i
				if record[i.to_i]==nil
					record[i.to_i]=1
				else
					record[i.to_i] = record[i.to_i] + 1
				end
			end
		end
	end
end

record_hash = {}
record.each_with_index do |r,i|
	if r!=nil
		record_hash[i]=r
	end
end

# Get annotations with more than min_sam samples each
sorted = record_hash.sort_by{|k,v| v}
go_1000 = []
sorted.each do |k,v|
	if v>=min_sam
		go_1000 << k
	end
end

# Search for sequences with the go_1000 annotations and write to file
if !Dir.exist?("out")
	Dir.mkdir("out")
end
if !Dir.exist?("out/train")
	Dir.mkdir("out/train")
end
if !Dir.exist?("out/test")
	Dir.mkdir("out/test")
end
train_seq_f = File.new("out/train/sequences.txt","w")
train_len_f = File.new("out/train/lengths.txt","w")
train_go_f = File.new("out/train/go_classes.txt","w")
test_seq_f = File.new("out/test/sequences.txt","w")
test_len_f = File.new("out/test/lengths.txt","w")
test_go_f = File.new("out/test/go_classes.txt","w")

# This is where we enforce the minimum number of samples for that GO annotation
num_sam = 0
go_sam = []
data_f = []
data.each_with_index do |d,i|
	k = d[2].to_i
	l = d[1].to_i
	if record_hash[k] >= min_sam
		data_f << d
		#seq_f.puts(d[0]+"\n")
		#len_f.puts(d[1]+"\n")
		#go_f.puts(d[2]+"\n")
		num_sam = num_sam + 1
		go_sam << d[2] # Keeping track of all GO annotations; will tally at end
	end
end

# Randomize our selection, split, and write to respective files
s = (data_f.length*0.8).ceil
train = data_f.slice(0,s)
test = data_f.slice(s+1,data_f.length-1)
tr = *(0..train.length-1)
tr.shuffle!
te = *(0..test.length-1)
te.shuffle!

# GO functions in list format
for i in 0..tr.length-1
	train_seq_f.puts(train[tr[i]][0]+"\n")
	train_len_f.puts(train[tr[i]][1]+"\n")
	train_go_f.puts(train[tr[i]][2]+"\n")
end
for i in 0..te.length-1
	test_seq_f.puts(test[te[i]][0]+"\n")
	test_len_f.puts(test[te[i]][1]+"\n")
	test_go_f.puts(test[te[i]][2]+"\n")
end

# Separate record for each GO function
#train.each_with_index do |d,i|
#	train_seq_f.puts(d[0]+"\n")
#	train_len_f.puts(d[1]+"\n")
#	train_go_f.puts(d[2]+"\n")
#end
#test.each do |d|
#	test_seq_f.puts(d[0]+"\n")
#	test_len_f.puts(d[1]+"\n")
#	test_go_f.puts(d[2]+"\n")
#end

puts "Number of samples: " + num_sam.to_s
puts "Number of classes: " + go_sam.group_by{|e| e}.map{|k, v| [k, v.length]}.length.to_s
puts "D2 python hash:"
thing = go_sam.group_by{|e| e}.map{|k, v| [k, v.length]}
thing.each_with_index do |v,i|
	print "\'"+v[0]+"\': "+i.to_s+","
end
puts ""

train_seq_f.close
train_len_f.close
train_go_f.close
test_seq_f.close
test_len_f.close
test_go_f.close
