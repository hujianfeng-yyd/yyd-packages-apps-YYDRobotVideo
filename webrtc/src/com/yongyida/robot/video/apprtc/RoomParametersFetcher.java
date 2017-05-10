/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.yongyida.robot.video.apprtc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import com.yongyida.robot.video.apprtc.AppRTCClient.SignalingParameters;
import com.yongyida.robot.video.apprtc.util.AsyncHttpURLConnection;
import com.yongyida.robot.video.apprtc.util.AsyncHttpURLConnection.AsyncHttpEvents;
import com.yongyida.robot.video.comm.log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * AsyncTask that converts an AppRTC room URL into the set of signaling
 * parameters to use with that room.
 */
public class RoomParametersFetcher {
	private static final String TAG = RoomParametersFetcher.class.getSimpleName();
	private static final int TURN_HTTP_TIMEOUT_MS = 5000;
	private final RoomParametersFetcherEvents events;
	private final String roomUrl;
	private final String roomMessage;
	private AsyncHttpURLConnection httpConnection;

	/**
	 * Room parameters fetcher callbacks.
	 */
	public interface RoomParametersFetcherEvents {
		/**
		 * Callback fired once the room's signaling parameters
		 * SignalingParameters are extracted.
		 */
		void onSignalingParametersReady(final SignalingParameters params);

		/**
		 * Callback for room parameters extraction error.
		 */
		void onSignalingParametersError(final String description);
	}

	public RoomParametersFetcher(String roomUrl, String roomMessage, final RoomParametersFetcherEvents events) {
		this.roomUrl = roomUrl;
		this.roomMessage = roomMessage;
		this.events = events;
	}

	public void makeRequest() {
		log.d(TAG, "Connecting to room: " + roomUrl);
		httpConnection = new AsyncHttpURLConnection("POST", roomUrl, roomMessage, new AsyncHttpEvents() {
			@Override
			public void onHttpError(String errorMessage) {
				log.e(TAG, "Room connection error: " + errorMessage);
				events.onSignalingParametersError(errorMessage);
			}

			@Override
			public void onHttpComplete(String response) {
				roomHttpResponseParse(response);
			}
		});
		httpConnection.send();
	}

	private void roomHttpResponseParse(String response) {
		log.d(TAG, "Room response: " + response);
		try {
			LinkedList<IceCandidate> iceCandidates = null;
			SessionDescription offerSdp = null;
			JSONObject roomJson = new JSONObject(response);

			String result = roomJson.getString("result");
			if (!result.equals("SUCCESS")) {
				events.onSignalingParametersError("Room response error: " + result);
				return;
			}
			response = roomJson.getString("params");
			roomJson = new JSONObject(response);
			String roomId = roomJson.getString("room_id");
			String clientId = roomJson.getString("client_id");
			String wssUrl = roomJson.getString("wss_url");
			String wssPostUrl = roomJson.getString("wss_post_url");
			boolean initiator = (roomJson.getBoolean("is_initiator"));
			if (!initiator) {
				iceCandidates = new LinkedList<IceCandidate>();
				String messagesString = roomJson.getString("messages");
				JSONArray messages = new JSONArray(messagesString);
				for (int i = 0; i < messages.length(); ++i) {
					String messageString = messages.getString(i);
					JSONObject message = new JSONObject(messageString);
					String messageType = message.getString("type");
					log.d(TAG, "GAE->C #" + i + " : " + messageString);
					if (messageType.equals("offer")) {
						offerSdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm(messageType),
								message.getString("sdp"));
					}
					else if (messageType.equals("candidate")) {
						IceCandidate candidate = new IceCandidate(message.getString("id"), message.getInt("label"),
								message.getString("candidate"));
						iceCandidates.add(candidate);
					}
					else {
						log.e(TAG, "Unknown message: " + messageString);
					}
				}
			}
			log.d(TAG, "RoomId: " + roomId + ". ClientId: " + clientId);
			log.d(TAG, "Initiator: " + initiator);
			log.d(TAG, "WSS url: " + wssUrl);
			log.d(TAG, "WSS POST url: " + wssPostUrl);

			LinkedList<PeerConnection.IceServer> iceServers = iceServersFromPCConfigJSON(
					roomJson.getString("pc_config"));
			boolean isTurnPresent = false;
			for (PeerConnection.IceServer server : iceServers) {
				log.d(TAG, "IceServer: " + server);
				if (server.uri.startsWith("turn:")) {
					isTurnPresent = true;
					break;
				}
			}
			// Request TURN servers.
			if (!isTurnPresent) {
				//LinkedList<PeerConnection.IceServer> turnServers = requestTurnServers(roomJson.getString("ice_server_url"));
				LinkedList<PeerConnection.IceServer> turnServers = requestTurnServers(roomJson.getString("turn_url"));
				for (PeerConnection.IceServer turnServer : turnServers) {
					log.d(TAG, "TurnServer: " + turnServer);
					iceServers.add(turnServer);
				}
			}

			SignalingParameters params = new SignalingParameters(iceServers, initiator, clientId, wssUrl, wssPostUrl,
					offerSdp, iceCandidates);
			events.onSignalingParametersReady(params);
		}
		catch (JSONException e) {
			events.onSignalingParametersError("Room JSON parsing error: " + e.toString());
		}
		catch (IOException e) {
			events.onSignalingParametersError("Room IO error: " + e.toString());
		}
	}

	// FOR ICE Server, not support 4G.
	// Requests & returns a TURN ICE Server based on a request URL.  Must be run
	// off the main thread!
	@SuppressWarnings("unused")
	private LinkedList<PeerConnection.IceServer> requestTurnServers_ICE(String url) throws IOException, JSONException {
		LinkedList<PeerConnection.IceServer> turnServers = new LinkedList<PeerConnection.IceServer>();
		log.d(TAG, "Request TURN from: " + url);
		
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setConnectTimeout(TURN_HTTP_TIMEOUT_MS);
		connection.setReadTimeout(TURN_HTTP_TIMEOUT_MS);
		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
			throw new IOException("Non-200 response when requesting TURN server from " + url + " : "
					+ connection.getHeaderField(null));
		}
		InputStream responseStream = connection.getInputStream();
		String response = drainStream(responseStream);
		connection.disconnect();
		log.d(TAG, "TURN response: " + response);
		
		JSONObject responseJSON = new JSONObject(response);
		JSONArray iceServers = responseJSON.getJSONArray("iceServers");
		for (int i = 0; i < iceServers.length(); ++i) {
			JSONObject server = iceServers.getJSONObject(i);
			JSONArray turnUrls = server.getJSONArray("urls");
			String username = server.has("username") ? server.getString("username") : "";
			String credential = server.has("credential") ? server.getString("credential") : "";
			for (int j = 0; j < turnUrls.length(); j++) {
				String turnUrl = turnUrls.getString(j);
				turnServers.add(new PeerConnection.IceServer(turnUrl, username, credential));
			}
		}
		return turnServers;
	}
	
	//FOR TURN_URL
	private LinkedList<PeerConnection.IceServer> requestTurnServers(String url) throws IOException, JSONException {
		log.d(TAG, "Request TURN from: " + url);

		LinkedList<PeerConnection.IceServer> turnServers = new LinkedList<PeerConnection.IceServer>();
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setConnectTimeout(TURN_HTTP_TIMEOUT_MS);
		connection.setReadTimeout(TURN_HTTP_TIMEOUT_MS);
		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
			throw new IOException("Non-200 response when requesting TURN server from " + url + " : "
					+ connection.getHeaderField(null));
		}
		InputStream responseStream = connection.getInputStream();
		String response = drainStream(responseStream);
		connection.disconnect();

		// 接收TURN响应, {"username":"1489111928:32780736","password":"Q5d4XOlLa6gsElxhAAVsUvJ0ygQ=","ttl":600,"uris":["turn:120.76.138.246:3478?transport=udp","turn:120.76.138.246:3478?transport=tcp","turn:120.76.138.246:3479?transport=udp","turn:120.76.138.246:3479?transport=tcp"]}
		log.d(TAG, "TURN response: " + response);
		JSONObject responseJSON = new JSONObject(response);
		String username = responseJSON.getString("username");
		String password = responseJSON.getString("password");
		JSONArray turnUris = responseJSON.getJSONArray("uris");
		for (int i = 0; i < turnUris.length(); i++) {
			String uri = turnUris.getString(i);
			turnServers.add(new PeerConnection.IceServer(uri, username, password));
		}
		return turnServers;
	}

	// Return the list of ICE servers described by a WebRTCPeerConnection
	// configuration string.
	private LinkedList<PeerConnection.IceServer> iceServersFromPCConfigJSON(String pcConfig) throws JSONException {
		JSONObject json = new JSONObject(pcConfig);
		JSONArray servers = json.getJSONArray("iceServers");
		LinkedList<PeerConnection.IceServer> ret = new LinkedList<PeerConnection.IceServer>();
		for (int i = 0; i < servers.length(); ++i) {
			JSONObject server = servers.getJSONObject(i);
			String url = server.getString("urls");
			String credential = server.has("credential") ? server.getString("credential") : "";
			ret.add(new PeerConnection.IceServer(url, "", credential));
		}
		return ret;
	}

	// Return the contents of an InputStream as a String.
	@SuppressWarnings("resource")
	private static String drainStream(InputStream in) {
		Scanner s = new Scanner(in).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
}
