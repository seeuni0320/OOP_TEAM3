package model;

import java.io.*;
import java.util.*;

public class StudyCafeRepository {
    private HashMap<String, User> userMap;
    private ArrayList<Seat> seatList;

    private final String USER_FILE = "users.txt";
    private final String SEAT_FILE = "seats.txt";

    public StudyCafeRepository() {
        this.userMap = new HashMap<>();
        this.seatList = new ArrayList<>();
        loadData();
    }

    public HashMap<String, User> getUserMap() { return userMap; }
    public ArrayList<Seat> getSeatList() { return seatList; }

    public synchronized User findUser(String phoneNumber) {
        return userMap.get(phoneNumber);
    }

    public synchronized void saveUser(User user) {
        userMap.put(user.getPhoneNumber(), user);
    }

    public void loadData() {
        // 1. 회원 정보 로드 (형식 변경 적용)
        File userFile = new File(USER_FILE);
        if (userFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    // 새 포맷: 번호, 회원여부, 남은분, 시작시간, 종료시간
                    if (data.length >= 5) {
                        User user = new User(data[0]);
                        user.setMember(Boolean.parseBoolean(data[1]));
                        user.setRemainingMinutes(Integer.parseInt(data[2]));
                        user.setPeriodStartTime(Long.parseLong(data[3]));
                        user.setPeriodEndTime(Long.parseLong(data[4]));
                        userMap.put(user.getPhoneNumber(), user);
                    }
                }
            } catch (Exception e) {
                System.out.println("회원 정보 로드 중 오류: " + e.getMessage());
            }
        }

        // 2. 좌석 정보 로드 (16석 렌더링 유지)
        File seatFile = new File(SEAT_FILE);
        Seat[] tempSeats = new Seat[17];

        if (seatFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(seatFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length >= 2) {
                        int seatNum = Integer.parseInt(data[0]);
                        if (seatNum < 1 || seatNum > 16) continue;

                        boolean occupied = Boolean.parseBoolean(data[1]);
                        Seat seat = new Seat(seatNum);
                        seat.setOccupied(occupied);
                        if (occupied && data.length == 3) {
                            seat.setAssignedUserPhone(data[2]);
                        }
                        tempSeats[seatNum] = seat;
                    }
                }
            } catch (Exception e) {
                System.out.println("좌석 정보 로드 중 오류: " + e.getMessage());
            }
        }
        
        for (int i = 1; i <= 16; i++) {
            if (tempSeats[i] != null) seatList.add(tempSeats[i]);
            else seatList.add(new Seat(i));
        }
    }

    public synchronized void saveData() {
        // 시간이 다 끝난 "비회원" 데이터 자동 삭제 로직
        // 먼저 현재 자리에 앉아있는 사람 목록을 뽑는다 (자리에 앉아있으면 삭제 안 함)
        HashSet<String> sittingUsers = new HashSet<>();
        for (Seat seat : seatList) {
            if (seat.isOccupied() && seat.getAssignedUserPhone() != null) {
                sittingUsers.add(seat.getAssignedUserPhone());
            }
        }

        List<String> toRemove = new ArrayList<>();
        for (User user : userMap.values()) {
            // 조건: 비회원(isMember=false) && 이용권 없음(hasUsableBalance=false) && 자리에도 안 앉아있음
            if (!user.isMember() && !user.hasUsableBalance() && !sittingUsers.contains(user.getPhoneNumber())) {
                toRemove.add(user.getPhoneNumber());
            }
        }
        // 조건에 맞는 비회원들을 userMap에서 흔적도 없이 날려버립니다.
        for (String phone : toRemove) {
            userMap.remove(phone);
        }

        // 1. 회원 정보 저장 (users.txt)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (User user : userMap.values()) {
                String line = String.format("%s,%b,%d,%d,%d",
                        user.getPhoneNumber(),
                        user.isMember(),
                        user.getRemainingMinutes(),
                        user.getPeriodStartTime(),
                        user.getPeriodEndTime());
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("회원 파일 저장 오류: " + e.getMessage());
        }

        // 2. 좌석 정보 저장 (seats.txt)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SEAT_FILE))) {
            for (Seat seat : seatList) {
                String line = String.format("%d,%b,%s",
                        seat.getSeatNumber(),
                        seat.isOccupied(),
                        seat.getAssignedUserPhone() == null ? "" : seat.getAssignedUserPhone());
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("좌석 파일 저장 오류: " + e.getMessage());
        }
    }
}