/**
 * Copyright [2012] [Datasalt Systems S.L.]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package storm.starter.spout;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.io.*;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import storm.starter.spout.SimpleSpout;
import storm.starter.common.Entry;
import storm.starter.common.XmlParser;

/**
 * The feed Spout extends {@link SimpleSpout} and emits Feed URLs to be fetched by {@link FetcherBolt} instances.
 * 
 * @author pere
 * 
 */
@SuppressWarnings("rawtypes")
public class FeedSpout extends SimpleSpout {

  SpoutOutputCollector _collector;
  private static final long serialVersionUID = 1L;
  Queue<Object> feedQueue = new LinkedList<Object>();
  String chkTime;

	public FeedSpout() {
		chkTime="";
	}

	@Override
	public void nextTuple() {
		Object nextFeed = feedQueue.poll();
    if(nextFeed != null) {
      _collector.emit(new Values(nextFeed), nextFeed);
    }
	}

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector){
		super.open(conf, context, collector);
    _collector = collector;
    Utils.sleep(3600000); //every one hour
    try{
    Process processObj = Runtime.getRuntime().exec("cd /home/radhika/svnrepo/storm-nodejs-redis-realtime-analytics/oacurl/ sh run.sh"); 
    InputStream stdin = processObj.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
			String xmlString = null;
			String channel = "GoogleAlerts";
			String line;
			String[] feeds;
			while ((line = reader.readLine()) != null) {
			      xmlString = xmlString + line;
			    }
			feeds = XmlParser.parser(xmlString, chkTime, channel );
			chkTime = feeds[0];
				for(String feed: feeds) {
					feedQueue.add(feed);
				}
			
    }catch(Exception e)
    {
    	//
    }
	}
	

	@Override
	public void ack(Object feedId) {
		feedQueue.add((String) feedId);
	}

	@Override
	public void fail(Object feedId) {
		feedQueue.add((String) feedId);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("feed"));
	}
}