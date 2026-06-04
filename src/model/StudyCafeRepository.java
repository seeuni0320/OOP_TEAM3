package model;

import java.io.*;
import java.util.*;

public class StudyCafeRepository {
    // 캡슐화 필드 선언
    private HashMap<String, User> userMap;
    private ArrayList<Seat> seatList;

    // 파일 저장 경로 정의
    private final String USER_FILE = "users.txt";
    private final String SEAT_FILE = "seats.txt";

    public StudyCafeRepository() {
        this.userMap = new HashMap<>();
        this.seatList = new ArrayList<>();
        loadData(); // 파일에서 데이터 읽어오기
    }

    // 데이터 조회 및 제공 메서드 
    public HashMap<String, User> getUserMap() {
        return userMap;
    }

    public ArrayList<Seat> getSeatList() {
        return seatList;
    }

    // [수정 1] 스케줄러와 충돌 방지를 위한 synchronized 락 추가
    public synchronized User findUser(String phoneNumber) {
        return userMap.get(phoneNumber);
    }

    // [수정 2] 신규 회원 저장 시 동시성 락 추가
    public synchronized void saveUser(User user) {
        userMap.put(user.getPhoneNumber(), user);
    }

    // 파일 입출력(File I/O) 로직 (txt 파일 읽기/쓰기)

    // 프로그램 시작 시 파일에서 데이터를 읽어와 컬렉션에 담는 메서드
    public void loadData() {
        // 1. 회원 정보 로드 (users.txt)
        File userFile = new File(USER_FILE);
        if (userFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // 데이터 포맷 예시: 01012345678,true,Time,2,0
                    String[] data = line.split(",");
                    if (data.length == 5) {
                        String phone = data[0];
                        boolean hasTicket = Boolean.parseBoolean(data[1]);
                        String type = data[2];
                        int hours = Integer.parseInt(data[3]);
                        int days = Integer.parseInt(data[4]);

                        User user = new User(phone);
                        user.setHasActiveTicket(hasTicket);
                        user.setActiveTicketType(type);
                        
                        // [수정 3] add 대신 순수 setter 사용 (부수효과 차단)
                        user.setRemainingHours(hours);
                        user.setRemainingDays(days);

                        userMap.put(phone, user);
                    }
                }
            } catch (IOException e) {
                System.out.println("회원 정보 파일 로드 중 오류 발생: " + e.getMessage());
            }
        }

        // 2. 좌석 정보 로드 (seats.txt)
        File seatFile = new File(SEAT_FILE);
        Seat[] tempSeats = new Seat[17]; // 임시 배열 (1~16번 사용)

        if (seatFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(seatFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // 데이터 포맷 예시: 1,true,01012345678
                    String[] data = line.split(",");
                    if (data.length >= 2) {
                        int seatNum = Integer.parseInt(data[0]);
                        
                        // 파일에 번호가 16번을 초과하는 잘못된 데이터가 있으면 무시
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
            } catch (IOException e) {
                System.out.println("좌석 정보 파일 로드 중 오류 발생: " + e.getMessage());
            }
        }
        
        //[수정 4] 파일이 훼손되었거나 일부만 있어도 무조건 16칸 꽉 채워서 보정
        for (int i = 1; i <= 16; i++) {
            if (tempSeats[i] != null) {
                seatList.add(tempSeats[i]);
            } else {
                seatList.add(new Seat(i)); // 빠진 번호는 새 빈자리로 채움
            }
        }
    }

    // 프로그램 종료 시 최신 메모리 데이터를 파일에 덮어쓰는 메서드
    // [수정 5] 저장할 때도 락을 걸어서 스레드 충돌 방지
    public synchronized void saveData() {
        // 1. 회원 정보 저장 (users.txt)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (User user : userMap.values()) {
                String line = String.format("%s,%b,%s,%d,%d",
                        user.getPhoneNumber(),
                        user.isHasActiveTicket(),
                        user.getActiveTicketType(),
                        user.getRemainingHours(),
                        user.getRemainingDays());
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("회원 정보 파일 저장 중 오류 발생: " + e.getMessage());
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
            System.out.println("좌석 정보 파일 저장 중 오류 발생: " + e.getMessage());
        }
    }
}